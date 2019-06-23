package io.vertx.apigateway.component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Gateway verticle class that hold the main functionality and 'glue' all the components together
 * @author fransiskus.prayuda
 */
public class ApiGatewayVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(ApiGatewayVerticle.class);
	private static final int DEFAULT_PORT = 11000;
	private static final String DEFAULT_HOSTNAME = "localhost";

	@Override
	public void start(final Future<Void> fut) {
		// get HTTP host and port from configuration, or use default value
		String host = config().getString("http.address", DEFAULT_HOSTNAME);
		int port = config().getInteger("http.port", DEFAULT_PORT);
		
		//fetch service discovery utility instance for this Vertx
		GatewayServiceDiscoveryUtils serviceDiscoveryUtils = GatewayServiceDiscoveryUtils.serviceDiscoveryFactory(this.vertx);
		//register all the available apis via the utility instance
		serviceDiscoveryUtils.publishHttpEndpoint("Get Employee Info", DEFAULT_HOSTNAME, 9000, "/employee");

		vertx
		.createHttpServer()
		//create router instance and pass the method handleRequest in the Router obj to the requestHandler
		.requestHandler(new GatewayRouter(this.getVertx())::handleRequest)
		.listen(port, host, result -> {
			if (result.succeeded()) {
				fut.complete();
				logger.info("API Gateway is running");
			} 
			else {
				logger.error(fut.cause());
				fut.fail(result.cause());
			}
		});
	}
}
