package io.vertx.apigateway.component;

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
	private Router router;

	public GatewayRouter(final Vertx vertx)
	{
		//Create new router instance
		router = Router.router(vertx);
		createRouting();
	}

	/**
	 * Handler for routing HTTP request to the correct client
	 * @param req {@code HttpServerRequest} object that need to be routed by the router
	 */
	public void handleRequest(HttpServerRequest req)
	{
		router.handle(req);
	}

	private void createRouting() {
		//push the request body and put it into RoutingContext
		router.route().handler(BodyHandler.create());

		logger.info("add route for path : /");
		router.route("/").handler(this::dispatchRequest);

		logger.info("add route for path : /getEmployeeInfo");
		router.route("/getEmployeeInfo").handler(this::dispatchRequest);
	}

	@SuppressWarnings({"deprecation"})
	private void dispatchRequest(RoutingContext routingContext)
	{
		final ServiceReference apiEndpointReference = GatewayServiceDiscoveryUtils.getAPIEndpointsURI(routingContext.request().absoluteURI());
		//assign HTTP api client where the request will be routed to
		final HttpClient apiClient = apiEndpointReference.getAs(HttpClient.class);
		apiClient.request(routingContext.request().method(), routingContext.request().uri(), 
				response ->  response.bodyHandler( body -> {
					HttpServerResponse toRsp = routingContext.response().setStatusCode(response.statusCode());
					 // send response
					toRsp.end(body);
				})) //request method causing depreceation warnings, supressed for now and will be updated to new method with upcoming version of vert.x
		.end();
	}
}
