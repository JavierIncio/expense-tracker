package com.exptrack.identity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Integration tests for the Identity Service application.
 *
 * <p>This test class uses Testcontainers to spin up a PostgreSQL database for testing purposes.
 * It verifies that the Spring application context loads successfully with the configured properties.</p>
 */
@Testcontainers
@SpringBootTest
class IdentityServiceApplicationTests {

    /**
     * A Testcontainers PostgreSQL container that provides a temporary PostgreSQL database for testing.
     *
     * <p>The container is configured to use the "postgres:16-alpine" image.</p>
     */
    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");

    /**
     * Dynamically sets the Spring application properties for the test context.
     *
     * <p>This method is called by Spring to register properties before the application
     * context is loaded. The secret is different from the production one.</p>
     *
     * @param r the DynamicPropertyRegistry used to register properties
     */
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("app.jwt.secret", () -> "s60xu8GG5YViQOjuzhIRv9WLKmqmd10tgYjyP9YuhPahleS2Gzpxpn6gmax+3w9k");
    }

	@Test void contextLoads() {}

}
