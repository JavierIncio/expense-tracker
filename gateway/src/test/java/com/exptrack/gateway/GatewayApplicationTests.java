package com.exptrack.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for the Gateway Service application.
 *
 * <p>This test class verifies that the Spring application context loads successfully with
 * the configured properties. The secret is different from the production one.</p>
 */
@SpringBootTest(properties = "app.jwt.secret=s60xu8GG5YViQOjuzhIRv9WLKmqmd10tgYjyP9YuhPahleS2Gzpxpn6gmax+3w9k")
class GatewayApplicationTests {

	@Test void contextLoads() {}

}
