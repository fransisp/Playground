package io.vertx.apigateway;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
public class ApiGatewayVerticleTest {

	@Test
	@DisplayName("A first test")
	void test_my_application(Vertx vertx, VertxTestContext testContext) {
		vertx.deployVerticle(new ApiGatewayVerticle(), testContext.succeeding(id -> {
			WebClient client = WebClient.create(vertx);

			client.get(8080, "localhost", "/").send(testContext.succeeding(response -> testContext.verify(() -> {
				assertThat(response.body().toString(), is(equalTo("Test")));
				testContext.completeNow();
			})));
		}));
	}
}
