package com.exptrack.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayRoutesConfigTest {

	private final GatewayRoutesConfig config = new GatewayRoutesConfig();

	private RouterFunction<ServerResponse> routes() {
		return config.gatewayRoutes("http://localhost:8081", "http://localhost:8082", "http://localhost:8083");
	}

	private boolean matches(String path) {
		return routes().route(ServerRequest.create(new MockHttpServletRequest("GET", path), List.of())).isPresent();
	}

	@Test
	void matchesIdentityServiceRoutes() {
		assertThat(matches("/api/auth/login")).isTrue();
		assertThat(matches("/api/auth/refresh")).isTrue();
	}

	@Test
	void matchesExpenseServiceRoutes() {
		assertThat(matches("/api/transactions/123")).isTrue();
		assertThat(matches("/api/categories")).isTrue();
		assertThat(matches("/api/budgets")).isTrue();
		assertThat(matches("/api/summary/monthly")).isTrue();
	}

	@Test
	void matchesNotificationServiceRoutes() {
		assertThat(matches("/api/notifications")).isTrue();
	}

	@Test
	void doesNotMatchUnknownPath() {
		assertThat(matches("/api/unknown")).isFalse();
	}
}