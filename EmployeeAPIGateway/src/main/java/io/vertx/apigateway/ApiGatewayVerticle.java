package io.vertx.apigateway;

import io.vertx.apigateway.component.GatewayRouter;
import io.vertx.apigateway.component.GatewayServiceDiscovery;
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

	private GatewayServiceDiscovery apiServiceDiscovery;

	@Override
	public void start(Future<Void> fut) {
		// get HTTP host and port from configuration, or use default value
		String host = config().getString("http.address", DEFAULT_HOSTNAME);
		int port = config().getInteger("http.port", DEFAULT_PORT);

		apiServiceDiscovery = new GatewayServiceDiscovery(this.vertx, config());
		//publish employee api
		apiServiceDiscovery.publishHttpEndpoint("Get Employee Info", "api-specific-host", 8081, "/getEmployeeInfo");
		apiServiceDiscovery.publishHttpEndpoint("Blank message", "api-specific-host", 8081, "/");
		
		vertx
		.createHttpServer()
		.requestHandler(req -> new GatewayRouter(this.vertx, this.apiServiceDiscovery.getAPIEndpoints(req.absoluteURI())).handleRequest(req))
		.listen(port, host, result -> {
			if (result.succeeded()) {
				fut.complete();
				logger.info("API Gateway is running");
			} else 
				fut.fail(result.cause());
			}
		);
	}
}
