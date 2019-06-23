package io.vertx.apigateway.component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.types.HttpEndpoint;

/**
 * Utility class to help manage the {@code ServiceDiscovery} object functionality
 * @author fransiskus.prayuda
 */
public class GatewayServiceDiscoveryUtils {
	
	private final ServiceDiscovery serviceDiscovery;
	private static final Logger logger = LoggerFactory.getLogger(GatewayServiceDiscoveryUtils.class);
	
	private Pattern portPattern = Pattern.compile("\\:[0-9]{1,5}");
	
	private GatewayServiceDiscoveryUtils (final Vertx gatewayVertx)
	{
		this.serviceDiscovery = ServiceDiscovery.create(gatewayVertx);
	}
	
	/**
	 * Factory to initialize the {@code ServiceDiscovery} instance in the Utility class with the given gateway vertx object
	 * @param gatewayVertx specify Vertx object where the Service Discovery is referenced
	 * @return an initialized utility object with referenced to service discovery object of the specified Vertx
	 */
	public static GatewayServiceDiscoveryUtils serviceDiscoveryFactory (final Vertx gatewayVertx)
	{
		return new GatewayServiceDiscoveryUtils(gatewayVertx);
	}

	/**
	 * Publish HTTP endpoint to the service record
	 * @param name of the endpoint
	 * @param host name
	 * @param port where the endpoint is listening to
	 * @param endpointName recorded in the service record
	 * @return {@code Future} object to let caller method know if the operation is succeded or not
	 */
	public Future<Void> publishHttpEndpoint(final String name, final String host, final int port, final String endpointName) {
		Record record = HttpEndpoint.createRecord(name, host, port, "/",
				new JsonObject().put("api.name", endpointName)
				);
		return publish(record);
	}
	
	private Future<Void> publish(final Record record) {
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
	 * @param String request URI
	 * @return 
	 */
	public ServiceReference getAPIEndpointHTTPClient(final String requestURI) {
		Matcher matcher = portPattern.matcher(requestURI);
		int startLocApiName = 0;
		if (matcher.find())
			startLocApiName = matcher.end();
		int endLocApiName = requestURI.lastIndexOf('/');
		String apiNameRequest = requestURI.substring(startLocApiName, endLocApiName);
		logger.debug("Fetch HTTP client for URI " + apiNameRequest);
		
		return filterHttpEndpointBasedOnRequestURI(apiNameRequest);
	}

	private ServiceReference filterHttpEndpointBasedOnRequestURI(String apiNameRequest) {
		Future<List<Record>> futureEndpointList = getAllHttpEndpoint();
		
		if (futureEndpointList.failed())
			logger.error(futureEndpointList.cause());
		else
		{
			//filter all endpointlist based on URI on the incoming request and return the reference
			Optional<Record> client = futureEndpointList.result().stream().filter(record -> record.getMetadata().getString("api.name").equals(apiNameRequest)).findAny();
			if (client.isPresent())
				return serviceDiscovery.getReference(client.get());
		}
		return null;
	}
	
	private Future<List<Record>> getAllHttpEndpoint ()
	{
		Future<List<Record>> future = Future.future();
		//fetch all available HTTP endpoint registered in service discovery
		serviceDiscovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE), future);
		return future;
	}
	
	/**
	 * Release given api end point reference after use
	 * @param apiEndpointReference resource that will be released after service call
	 */
	public void releaseAPIEndpointsURI(final ServiceReference apiEndpointReference) {	
		serviceDiscovery.release(apiEndpointReference);
	}
}
