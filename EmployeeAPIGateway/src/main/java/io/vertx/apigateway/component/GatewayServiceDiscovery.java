package io.vertx.apigateway.component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.HttpEndpoint;

public class GatewayServiceDiscovery {

	private static final Logger logger = LoggerFactory.getLogger(GatewayServiceDiscovery.class);

	private ServiceDiscovery discovery;
	private Set<Record> records = new ConcurrentHashSet<>();

	public GatewayServiceDiscovery()
	{
		throw new UnsupportedOperationException();
	}

	public GatewayServiceDiscovery(final Vertx vertx, JsonObject configuration)
	{
		// init service discovery instance
		discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(configuration));
	}

	/**
	 * Publish HTTP endpoint to the service record
	 * @param name
	 * @param host
	 * @param port
	 * @return
	 */
	public Future<Void> publishHttpEndpoint(String name, String host, int port, String endpointName) {
		Record record = HttpEndpoint.createRecord(name, host, port, "/",
				new JsonObject().put("api.name", endpointName)
				);
		return publish(record);
	}

	/**
	 * Get all REST endpoints from the service discovery
	 * @param string 
	 *
	 * @return async result
	 */
	public ServiceReference getAPIEndpoints(String requestURI) {
		Future<List<Record>> future = Future.future();
		discovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE), future);
		
		String apiNameRequest = requestURI.substring(requestURI.lastIndexOf('/'));
		logger.info("Fetch client for URI " + apiNameRequest);
		
		ServiceReference forwardAPI = null;
		if (future.succeeded())
		{
			Optional<Record> client = future.result().stream().filter(record -> record.getMetadata().getString("api.name").equals(apiNameRequest)).findAny();
			if (client.isPresent())
				forwardAPI = discovery.getReference(client.get());
		}
		return forwardAPI;
	}

	/**
	 * Publish a service with record.
	 *
	 * @param record service record
	 * @return async result
	 */
	private Future<Void> publish(Record record) {
		Future<Void> future = Future.future();
		// publish the service
		discovery.publish(record, ar -> {
			if (ar.succeeded()) {
				records.add(record);
				logger.info("Service <" + ar.result().getName() + "> succesfully published");
				future.complete();
			} else {
				future.fail(ar.cause());
			}
		});

		return future;
	}

}
