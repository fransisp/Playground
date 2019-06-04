package io.vertx.apigateway.component;

import java.util.List;
import java.util.Optional;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
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
	
	private Future<Void> publish(Record record) {
		Future<Void> future = Future.future();
		// publish the service
		discovery.publish(record, ar -> {
			if (ar.succeeded()) {
				logger.info("Service <" + ar.result().getName() + "> succesfully published");
				future.complete();
			} else {
				future.fail(ar.cause());
			}
		});

		return future;
	}

	/**
	 * Get all REST endpoints from the service discovery
	 * @param string 
	 *
	 * @return async result
	 */
	public ServiceReference getAPIEndpointsURI(String requestURI) {		
		String apiNameRequest = requestURI.substring(requestURI.lastIndexOf('/'));
		logger.info("Fetch HTTP client for URI " + apiNameRequest);
		
		return filterHttpEndpointBasedOnRequestURI(apiNameRequest);
	}

	private ServiceReference filterHttpEndpointBasedOnRequestURI(String apiNameRequest) {
		Future<List<Record>> futureEndpointList = getAllHttpEndpoint();
		ServiceReference serviceRefHolder = null;
		
		if (futureEndpointList.failed())
			logger.error(futureEndpointList.cause());
		else
		{
			//filter all endpointlist based on URI on the incoming request and return the reference
			Optional<Record> client = futureEndpointList.result().stream().filter(record -> record.getMetadata().getString("api.name").equals(apiNameRequest)).findAny();
			if (client.isPresent())
			{
				logger.info("Found record " + client.get().getLocation());
				serviceRefHolder =  discovery.getReference(client.get());
			}
		}
		return serviceRefHolder;
	}
	
	private Future<List<Record>> getAllHttpEndpoint ()
	{
		Future<List<Record>> future = Future.future();
		//fetch all available HTTP endpoint registered in service discovery
		discovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE), future);
		return future;
	}
}
