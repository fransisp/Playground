package io.vertx.apigateway.component;

import java.util.Objects;
import java.util.function.BiConsumer;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.ServiceReference;

public class GatewayRouter {

	private static final Logger logger = LoggerFactory.getLogger(GatewayRouter.class);
	private final Router router;

	private final GatewayServiceDiscoveryUtils serviceDiscoveryUtils;
	private final CircuitBreaker circuitBreaker;

	public GatewayRouter(final Vertx gatewayVertx)
	{
		//Create new router instance
		this.router = createRouting(gatewayVertx);
		serviceDiscoveryUtils = GatewayServiceDiscoveryUtils.serviceDiscoveryFactory(gatewayVertx);

		circuitBreaker = CircuitBreaker.create("my-circuit-breaker", gatewayVertx,
				// Configure it
				new CircuitBreakerOptions()
				.setTimeout(1000) // Operation timeout
				.setFallbackOnFailure(true) // Call the fallback on failure
				.setMaxFailures(3)
				.setResetTimeout(3000)) // Switch to the half-open state every 3s
				.openHandler(handler -> logger.info("Circuit is now open"));
	}

	/**
	 * Handler for routing HTTP request to the correct client
	 * @param req {@code HttpServerRequest} object that need to be routed by the router
	 */
	public void handleRequest(HttpServerRequest req)
	{
		router.handle(req);
	}

	private Router createRouting(final Vertx gatewayVertx) {
		Router initRouter = Router.router(gatewayVertx);
		//push the request body and put it into RoutingContext
		initRouter.route().handler(BodyHandler.create());
		//register handler to dispatch all requests
		initRouter.route("/*").handler(req -> this.passReqToCircuitBreaker(req, this::dispatchRequest));
		return initRouter;
	}

	@SuppressWarnings({"deprecation"})
	private void dispatchRequest(RoutingContext routingContext, Future<Object> operation) {
		final ServiceReference apiEndpointReference = serviceDiscoveryUtils.getAPIEndpointHTTPClient(routingContext.request().absoluteURI());
		if (Objects.isNull(apiEndpointReference)) 
			operation.fail("No HTTP endpoint can be found with URI");
		else {
			//assign HTTP api client where the request will be routed to
			final HttpClient apiClient = apiEndpointReference.getAs(HttpClient.class);
			//request method causing depreceation warnings, supressed for now and will be updated to new method with upcoming version of vert.x
			apiClient.request(routingContext.request().method(), routingContext.request().uri(), 
					response ->  response.bodyHandler( body -> {
						HttpServerResponse toRsp = routingContext.response().setStatusCode(response.statusCode());
						// send response
						toRsp.end(body);
						operation.complete();
					}))
			.exceptionHandler(operation::fail)
			.end();
			serviceDiscoveryUtils.releaseAPIEndpointsURI(apiEndpointReference);
		}
	}
	
	private void passReqToCircuitBreaker(RoutingContext routingContext, BiConsumer<RoutingContext, Future<Object>> requestDispatcher)
	{
		circuitBreaker.execute( operation -> {
			requestDispatcher.accept(routingContext, operation);
		}).setHandler(ar -> {
			if (ar.failed())
				handleBadGateway(routingContext);
		});
	}

	private void handleBadGateway(RoutingContext context) {
		context.response()
		.setStatusCode(502)
		.putHeader("content-type", "application/json")
		.end("Test Error: bad_gateway");
	}
}
