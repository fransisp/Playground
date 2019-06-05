package io.vertx.apigateway;

import io.vertx.apigateway.component.GatewayRouter;
import io.vertx.apigateway.component.GatewayServiceDiscoveryUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * https://www.sczyh30.com/vertx-blueprint-microservice/
 * https://www.eclipse.org/community/eclipse_newsletter/2016/october/article4.php
 * @author fransiskus.prayuda
 *
 */
public class ApiGatewayVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(ApiGatewayVerticle.class);
	private static final int DEFAULT_PORT = 8080;
	private static final String DEFAULT_HOSTNAME = "localhost";

	@Override
	public void start(Future<Void> fut) {
		// get HTTP host and port from configuration, or use default value
		String host = config().getString("http.address", DEFAULT_HOSTNAME);
		int port = config().getInteger("http.port", DEFAULT_PORT);
		
		 GatewayServiceDiscoveryUtils serviceDiscoveryUtils = GatewayServiceDiscoveryUtils.serviceDiscoveryFactory(this.vertx);
		
		//publish employee api
		 serviceDiscoveryUtils.publishHttpEndpoint("Get Employee Info", DEFAULT_HOSTNAME, 9000, "/getEmployeeInfo");
		 serviceDiscoveryUtils.publishHttpEndpoint("Test message", DEFAULT_HOSTNAME, 9000, "/");

		vertx
		.createHttpServer()
		//create router instance and pass the method handleRequest to the requestHandler
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
