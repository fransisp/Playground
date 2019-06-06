package io.vertx.apigateway.component;

import java.util.function.BiConsumer;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * Utility class to help manage the {@code CircuitBreaker} object functionality
 * @author fransiskus.prayuda
 */
public class GatewayCircuitBreakerUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(GatewayCircuitBreakerUtils.class);
	private final CircuitBreaker circuitBreaker;
	
	// Circuit breaker configurations
	private final CircuitBreakerOptions cbOptions = new CircuitBreakerOptions()
			.setTimeout(1000) // Operation timeout
			.setFallbackOnFailure(true) // Call the fallback on failure
			.setMaxFailures(3)
			.setResetTimeout(3000); // Switch to the half-open state every 3s
	
	private GatewayCircuitBreakerUtils (final Vertx gatewayVertx)
	{
		this.circuitBreaker = CircuitBreaker.create("gateway-circuit-breaker", gatewayVertx, cbOptions)
				.openHandler(handler -> logger.debug("Circuit is now open"))
				.closeHandler(handler -> logger.debug("Circuit is now closed again"));				
	}
	
	/**
	 * Factory to initialize the {@code CircuitBreaker} instance in the Utility class with the given gateway vertx object
	 * @param gatewayVertx specify Vertx object where the Circuit Breaker is referenced
	 * @return an initialized utility object with referenced to circuit breaker object of the specified Vertx
	 */
	public static GatewayCircuitBreakerUtils circuitBreakerFactory (final Vertx gatewayVertx)
	{
		return new GatewayCircuitBreakerUtils(gatewayVertx);
	}
	
	/**
	 * Pass the request to circuit breaker and if the circuit is closed, forward the request to the requestDispatcher
	 * @param routingContext object holding the request
	 * @param requestDispatcher method that will handle the request if circuit is closed
	 */
	public void passReqToCircuitBreaker(final RoutingContext routingContext, final BiConsumer<RoutingContext, Future<Object>> requestDispatcher)
	{
		circuitBreaker.execute( operation -> 
			requestDispatcher.accept(routingContext, operation)
		).setHandler(ar -> {
			if (ar.failed())
				handleBadGateway(routingContext);
		});
	}

	private void handleBadGateway(final RoutingContext context) {
		//standard response to be send back to client if the circuit open during request
		context.response()
		.setStatusCode(502)
		.putHeader("content-type", "application/json")
		.end("Test Error: bad_gateway");
	}
}
