package io.vertx.apigateway.component;

import java.util.Objects;

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

/**
 * Class that holds the main router functionality and its dependency utility objects {@code GatewayServiceDiscoveryUtils} and {@code GatewayCircuitBreakerUtils}
 * @author fransiskus.prayuda
 */
public class GatewayRouter {

	private static final Logger logger = LoggerFactory.getLogger(GatewayRouter.class);
	private final Router router;

	private final GatewayServiceDiscoveryUtils serviceDiscoveryUtils;
	private final GatewayCircuitBreakerUtils circuitBreakerUtils;

	/**
	 * Initiate router instance and all the dependency for the given gateway Vertx
	 * @param gatewayVertx
	 */
	public GatewayRouter(final Vertx gatewayVertx)
	{
		//Create new router instance
		this.router = createRouting(gatewayVertx);
		//fetch service discovery configured for the given Vertx
		serviceDiscoveryUtils = GatewayServiceDiscoveryUtils.serviceDiscoveryFactory(gatewayVertx);
		//fetch circuit breaker configured for the given Vertx
		circuitBreakerUtils = GatewayCircuitBreakerUtils.circuitBreakerFactory(gatewayVertx);
	}

	private Router createRouting(final Vertx gatewayVertx) {
		Router initRouter = Router.router(gatewayVertx);
		//push the request body and put it into RoutingContext
		initRouter.route().handler(BodyHandler.create());
		//register handler to dispatch all the requests
		initRouter.route("/*").handler(req -> circuitBreakerUtils.passReqToCircuitBreaker(req, this::dispatchRequest));
		return initRouter;
	}

	/**
	 * Handler for routing HTTP request to the correct client
	 * @param req {@code HttpServerRequest} object that need to be routed by the router
	 */
	public void handleRequest(HttpServerRequest req)
	{
		router.handle(req);
	}

	@SuppressWarnings({"deprecation"})
	private void dispatchRequest(RoutingContext routingContext, Future<Object> operation) {
		final ServiceReference apiEndpointReference = serviceDiscoveryUtils.getAPIEndpointHTTPClient(routingContext.request().absoluteURI());
		if (Objects.isNull(apiEndpointReference)) 
		{
			operation.fail("No HTTP endpoint can be found with URI" + routingContext.request().absoluteURI());
			logger.error(operation.cause());
		}
		else {
			//assign HTTP api client where the request will be routed to
			final HttpClient apiClient = apiEndpointReference.getAs(HttpClient.class);
			//request method causing depreceation warnings, supressed for now and will be updated to new method with upcoming version of vert.x
			apiClient.request(routingContext.request().method(), routingContext.request().uri(), 
					response ->  response.bodyHandler( body -> {
						HttpServerResponse toRsp = routingContext.response().setStatusCode(response.statusCode());
						// send response back to the requester
						toRsp.end(body);
						operation.complete();
					}))
			.exceptionHandler(operation::fail)
			.end();
			//release the service reference to free up object
			serviceDiscoveryUtils.releaseAPIEndpointsURI(apiEndpointReference);
		}
	}
}