package io.vertx.apigateway;

import java.util.Set;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.HttpEndpoint;

/**
 * https://www.sczyh30.com/vertx-blueprint-microservice/
 * @author fransiskus.prayuda
 *
 */
public class ApiGatewayVerticle extends AbstractVerticle {

	private static final Logger logger = LoggerFactory.getLogger(ApiGatewayVerticle.class);
	private static final int DEFAULT_PORT = 8080;
	private static final String DEFAULT_HOSTNAME = "localhost";

	private ServiceDiscovery discovery;
	protected Set<Record> records = new ConcurrentHashSet<>();

	// get HTTP host and port from configuration, or use default value
	String host = config().getString("api.gateway.http.address", DEFAULT_HOSTNAME);
	int port = config().getInteger("api.gateway.http.port", DEFAULT_PORT);

	@Override
	public void start(Future<Void> fut) {

		// init service discovery instance
		discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));

		vertx
		.createHttpServer()
		.requestHandler(r ->
		r.response().end("<h1>Hello from my first Vert.x application</h1>"))
		.listen(port, host, result -> {
			if (result.succeeded()) {
				fut.complete();
				logger.info("API Gateway is running");
			} else {
				fut.fail(result.cause());
			}
		});
	}

	/**
	 * Publish HTTP endpoint to the service record
	 * @param name
	 * @param host
	 * @param port
	 * @return
	 */
	protected Future<Void> publishHttpEndpoint(String name, String host, int port) {
		Record record = HttpEndpoint.createRecord(name, host, port, "/",
				new JsonObject().put("api.name", config().getString("api.name", ""))
				);
		return publish(record);
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
	        logger.info("Service <" + ar.result().getName() + "> published");
	        future.complete();
	      } else {
	        future.fail(ar.cause());
	      }
	    });

	return future;
	}
}
