package io.vertx.apigateway;

import io.vertx.apigateway.component.GatewayServiceDiscovery;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * https://www.sczyh30.com/vertx-blueprint-microservice/
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
		
		apiServiceDiscovery = new GatewayServiceDiscovery(this.vertx, config());
		
		// get HTTP host and port from configuration, or use default value
		String host = config().getString("api.gateway.http.address", DEFAULT_HOSTNAME);
		int port = config().getInteger("api.gateway.http.port", DEFAULT_PORT);

		//publish employee api
		apiServiceDiscovery.publishHttpEndpoint("getEmployeeInfo", host, port, config());

		vertx
		.createHttpServer()
		.requestHandler(r -> r.response().end("Test"))
		.listen(port, host, result -> {
			if (result.succeeded()) {
				apiServiceDiscovery.publishApiGateway(host, port);
				fut.complete();
				logger.info("API Gateway is running");
			} else {
				fut.fail(result.cause());
			}
		});
	}
}
