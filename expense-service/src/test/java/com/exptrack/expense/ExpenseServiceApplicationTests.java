package com.exptrack.expense;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Integration tests for the Expense Service application.
 *
 * <p>This test class uses Testcontainers to spin up a PostgreSQL database for testing purposes.
 * It verifies that the Spring application context loads successfully with the configured properties.</p>
 */
@Testcontainers
@SpringBootTest
class ExpenseServiceApplicationTests {

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
     * context is loaded.</p>
     *
     * @param r the DynamicPropertyRegistry used to register properties
     */
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

	@Test void contextLoads() {}

}
