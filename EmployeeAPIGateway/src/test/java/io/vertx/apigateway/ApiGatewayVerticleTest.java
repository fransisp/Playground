package io.vertx.apigateway;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.apigateway.component.ApiGatewayVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.cli.annotations.Description;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class ApiGatewayVerticleTest {
	
	private WebClient client;

	@BeforeAll
	@DisplayName("deploy the gateway verticle and create mock HTTP API server")
	static void setup (Vertx vertx, VertxTestContext testContext)
	{
		vertx.deployVerticle(new ApiGatewayVerticle(), testContext.completing());
		
		//start mock HTTP API server with predefined result for test
		vertx
		.createHttpServer()
		.requestHandler(req -> {
			if (req.uri().equalsIgnoreCase("/getemployeeinfo"))
				req.response().setStatusCode(200).end("TestRouting GetEmployeeInfo");
		  })
		.listen(9000, "localhost", result -> {
			if (result.failed()) {
				testContext.failNow(result.cause());
			}
			else
				testContext.completing();
		});
	}
	
	@BeforeEach
	void createWebClient (Vertx vertx)
	{
		client = WebClient.create(vertx);
	}

	@Test
	@DisplayName("A routing test")
	@Description("Happy flow test - pass the request from 8080 to the routed listener server on port 9000 and get the response back")
	void routing_test(VertxTestContext testContext) {
		client.get(8080, "localhost", "/getEmployeeInfo").send(testContext.succeeding(response -> testContext.verify(() -> {
			assertThat(response.body().toString(), is(equalTo("TestRouting GetEmployeeInfo")));
			testContext.completeNow();
			})));
		}
	
	@Test
	@DisplayName("A circuit breaker test")
	@Description("Resiliency test - the /getDepartmentInfo does not exist but it should fail gracefully "
			+ "since the exception will be handled by the circuit breaker")
	void when_routing_test_failure_then_error_handled_by_circuit_breaker(Vertx vertx, VertxTestContext testContext) {
		client.get(8080, "localhost", "/getDepartmentInfo").send(testContext.succeeding(response -> testContext.verify(() -> {
			assertThat(response.body().toString(), is(equalTo("Test Error: bad_gateway")));
			testContext.completeNow();
			})));
		}
}
