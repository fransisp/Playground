package io.vertx.apigateway.component;

import java.util.List;
import java.util.Optional;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.HttpEndpoint;

public enum GatewayServiceDiscoveryUtils {

	INSTANCE;
	
	private static ServiceDiscovery serviceDiscovery;
	private static final Logger logger = LoggerFactory.getLogger(GatewayServiceDiscoveryUtils.class);
	
	/**
	 * Initialize the singleton instance in the Utility class with the given parameter object
	 * @param discovery
	 * @throws IllegalAccessException thrown if the singleton instance already initialize
	 */
	public static void setDiscoveryReference (final ServiceDiscovery discovery) throws IllegalAccessException
	{
		if (serviceDiscovery == null)
			serviceDiscovery = discovery;
		else
			throw new IllegalAccessException("Service discovery variable is already initialized");
	}

	/**
	 * Publish HTTP endpoint to the service record
	 * @param name
	 * @param host
	 * @param port
	 * @return
	 */
	public static Future<Void> publishHttpEndpoint(String name, String host, int port, String endpointName) {
		Record record = HttpEndpoint.createRecord(name, host, port, "/",
				new JsonObject().put("api.name", endpointName)
				);
		return publish(record);
	}
	
	private static Future<Void> publish(Record record) {
		Future<Void> future = Future.future();
		// publish the service
		serviceDiscovery.publish(record, ar -> {
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
	public static ServiceReference getAPIEndpointsURI(String requestURI) {		
		String apiNameRequest = requestURI.substring(requestURI.lastIndexOf('/'));
		logger.info("Fetch HTTP client for URI " + apiNameRequest);
		
		return filterHttpEndpointBasedOnRequestURI(apiNameRequest);
	}

	private static ServiceReference filterHttpEndpointBasedOnRequestURI(String apiNameRequest) {
		Future<List<Record>> futureEndpointList = getAllHttpEndpoint();
		ServiceReference serviceRefHolder = null;
		
		if (futureEndpointList.failed())
			logger.error(futureEndpointList.cause());
		else
		{
			//filter all endpointlist based on URI on the incoming request and return the reference
			Optional<Record> client = futureEndpointList.result().stream().filter(record -> record.getMetadata().getString("api.name").equals(apiNameRequest)).findAny();
			if (client.isPresent())

				serviceRefHolder =  serviceDiscovery.getReference(client.get());
		}
		return serviceRefHolder;
	}
	
	private static Future<List<Record>> getAllHttpEndpoint ()
	{
		Future<List<Record>> future = Future.future();
		//fetch all available HTTP endpoint registered in service discovery
		serviceDiscovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE), future);
		return future;
	}
}
