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
	@Description("")
	void routing_test(VertxTestContext testContext) {
		client.get(8080, "localhost", "/getEmployeeInfo").send(testContext.succeeding(response -> testContext.verify(() -> {
			assertThat(response.body().toString(), is(equalTo("TestRouting GetEmployeeInfo")));
			testContext.completeNow();
			})));
		}
	
	@Test
	@DisplayName("A circuit breaker test")
	@Description("")
	void routing_test_failure(Vertx vertx, VertxTestContext testContext) {
		client.get(8080, "localhost", "/getDepartmentInfo").send(testContext.succeeding(response -> testContext.verify(() -> {
			assertThat(response.body().toString(), is(equalTo("Test Error: bad_gateway")));
			testContext.completeNow();
			})));
		}
}
