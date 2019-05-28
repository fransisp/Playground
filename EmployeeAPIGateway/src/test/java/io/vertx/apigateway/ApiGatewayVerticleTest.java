package io.vertx.apigateway;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;

@ExtendWith(VertxExtension.class)
public class ApiGatewayVerticleTest {

	private Vertx vertx;
	private VertxTestContext testContext;

	@BeforeEach
	public void setUp() {
		vertx = Vertx.vertx();
		testContext = new VertxTestContext();
	}

	@Test
	@DisplayName("A first test")
	void test_my_application() {
		vertx.deployVerticle(new ApiGatewayVerticle(), testContext.succeeding(id -> {
			WebClient client = WebClient.create(vertx);

			client.get(8080, "localhost", "/").send(testContext.succeeding(response -> testContext.verify(() -> {
				assertTrue((response.body()).toString().equalsIgnoreCase("Hello"));
				testContext.completeNow();
			})));
		}));
	}
}
