package com.exptrack.expense;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    // Generate a new user ID for each test to ensure isolation between tests
    protected final UUID userId = UUID.randomUUID();

    // --- Auth (real headers, passing through XUserAuthFilter) --------------------------------------------------------
    protected RequestPostProcessor asUser(UUID userId, String roles) {
        return request -> {
            request.addHeader("X-User-Id", userId.toString());
            request.addHeader("X-User-Email", "user" + userId + "@example.com");
            request.addHeader("X-User-Roles", roles);
            return request;
        };
    }

    protected RequestPostProcessor asUser(UUID userId) {
        return asUser(userId, "USER");
    }

    // --- Helpers JSON ------------------------------------------------------------------------------------------------

    protected String categoryBody(String name, String type) {
        return "{\"name\":\"" + name + "\",\"type\":\"" + type + "\"}";
    }

    protected String transactionBody(String type, String amount, String categoryId,
                                     String date, String description) {
        return "{\"type\":\"" + type + "\",\"amount\":" + amount
                + ",\"categoryId\":\"" + categoryId + "\",\"date\":\"" + date + "\""
                + (description == null ? "" : ",\"description\":\"" + description + "\"")
                + "}";
    }

    protected String budgetBody(String categoryId, int year, int month, String amount) {
        return "{\"categoryId\":\"" + categoryId + "\",\"year\":" + year
                + ",\"month\":" + month + ",\"amount\":" + amount + "}";
    }

    // --- Helpers -----------------------------------------------------------------------------------------------------
    protected UUID extractIdGivenCategory(String name, String type) throws Exception {
        return extractId(mockMvc.perform(post("/api/categories")
                .with(asUser(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(categoryBody(name, type)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    protected UUID extractId(String json) throws Exception {
        return UUID.fromString(objectMapper.readTree(json).get("id").asText());
    }

    protected JsonNode json(MockHttpServletResponse response) throws Exception {
        return objectMapper.readTree(response.getContentAsString());
    }

    protected MockHttpServletResponse createTransaction(UUID owner, String type, String amount,
                                                        String categoryId, String date, String description) throws Exception {
        return mockMvc.perform(post("/api/transactions")
                .with(asUser(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(transactionBody(type, amount, categoryId, date, description)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
    }

    protected MockHttpServletResponse createBudget(UUID owner, String categoryId, int year, int month,
                                                   String amount) throws Exception {
        return mockMvc.perform(post("/api/budgets")
                .with(asUser(owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content(budgetBody(categoryId, year, month, amount)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
    }
}
