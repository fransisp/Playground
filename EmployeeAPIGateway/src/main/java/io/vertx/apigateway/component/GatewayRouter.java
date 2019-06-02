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
	private HttpClient apiClient;

	public GatewayRouter()
	{
		throw new UnsupportedOperationException();
	}
	
	public GatewayRouter(final Vertx vertx, final ServiceReference apiClientReference)
	{
		logger.info("Create new router instance");
		router = Router.router(vertx);
		apiClient = apiClientReference.getAs(HttpClient.class);
		apiClientReference.release();
		createRouting();
	}
	
	public void handleRequest(HttpServerRequest req)
	{
		router.handle(req);
	}

	private void createRouting() {
		//push the request body and put it into routingContext
		router.route().handler(BodyHandler.create());
		
		logger.info("add route for path : /");
		router.route("/").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "application/json");
			response.setStatusCode(200);
			response.end("Test");
		});
		
		logger.info("add route for path : /getEmployeeInfo");
		router.route("/getEmployeeInfo").handler(this::dispatchRequest);
	}

	private void dispatchRequest(RoutingContext routingContext)
	{
		logger.info("Call will be routed to " + apiClient.toString());
		routingContext.response()
		.putHeader("content-type", "application/json")
		.setStatusCode(200)
		.end("TestRouting");
	}
}
