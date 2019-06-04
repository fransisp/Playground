package io.vertx.apigateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ApiMockServerVerticle extends AbstractVerticle {
	
	private static final Logger logger = LoggerFactory.getLogger(ApiMockServerVerticle.class);
	
	@Override
	public void start(Future<Void> fut) {
		vertx
		.createHttpServer()
		.requestHandler(req -> {
		    req.response().setStatusCode(200).end("TestRouting");
		  })
		.listen(9000, "localhost", result -> {
			if (result.succeeded()) {
				fut.complete();
				logger.info("Mock API server is running");
			} 
			else {
				fut.fail(result.cause());
				logger.error(fut.cause());
			}
		});
	}

}
