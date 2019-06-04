package io.vertx.apigateway;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class ApiGatewayVerticleTest {

	@BeforeEach
	void setup (Vertx vertx, VertxTestContext testContext)
	{
		vertx.deployVerticle(new ApiMockServerVerticle(), testContext.completing());
		vertx.deployVerticle(new ApiGatewayVerticle(), testContext.completing());
	}

	@Test
	@DisplayName("A sanity test")
	void test_my_application(Vertx vertx, VertxTestContext testContext) {
		WebClient client = WebClient.create(vertx);

		client.get(8080, "localhost", "/").send(testContext.succeeding(response -> testContext.verify(() -> {
			assertThat(response.body().toString(), is(equalTo("Test")));
			testContext.completeNow();
		})));
	}

	@Test
	@DisplayName("A routing test")
	void routing_test(Vertx vertx, VertxTestContext testContext) {
		
		WebClient client = WebClient.create(vertx);
		
		client.get(8080, "localhost", "/getEmployeeInfo").send(testContext.succeeding(response -> testContext.verify(() -> {
			assertThat(response.body().toString(), is(equalTo("TestRouting")));
			testContext.completeNow();
			})));
		}
}
