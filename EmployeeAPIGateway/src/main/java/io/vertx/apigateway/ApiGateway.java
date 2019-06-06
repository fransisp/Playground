package io.vertx.apigateway;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.vertx.apigateway.component.ApiGatewayVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;

public class ApiGateway {
	
	public static void main(String[] args) throws InterruptedException {
		//takes the application result into consideration and blocks the main thread until it succeeds:
		BlockingQueue<AsyncResult<String>> q = new ArrayBlockingQueue<>(1);
		Vertx.vertx().deployVerticle(new ApiGatewayVerticle(), q::offer);
		AsyncResult<String> result = q.take();
		if (result.failed()) {
		    throw new RuntimeException(result.cause());
		}
	}
}
