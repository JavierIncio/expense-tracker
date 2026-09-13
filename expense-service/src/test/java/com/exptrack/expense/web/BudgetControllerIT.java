package com.exptrack.expense.web;

import com.exptrack.expense.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BudgetControllerIT extends AbstractIntegrationTest {

    @Test
    void create_returns201() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");

        MockHttpServletResponse response = mockMvc.perform(post("/api/budgets")
                        .with(asUser(userId))
                        .contentType(APPLICATION_JSON)
                        .content(budgetBody(cat.toString(), 2026, 2, "100.00")))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        JsonNode budget = json(response);
        assertThat(budget.get("categoryId").asText()).isEqualTo(cat.toString());
        assertThat(budget.get("year").asInt()).isEqualTo(2026);
        assertThat(budget.get("month").asInt()).isEqualTo(2);
        assertThat(budget.get("amount").asText()).isEqualTo("100.0");
    }

    @Test
    void create_duplicate_returns409() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        createBudget(userId, cat.toString(), 2026, 2, "100.00");

        mockMvc.perform(post("/api/budgets")
                        .with(asUser(userId))
                        .contentType(APPLICATION_JSON)
                        .content(budgetBody(cat.toString(), 2026, 2, "150.00")))
                .andExpect(status().isConflict());
    }

    @Test
    void duplicate_sameCategory_otherUser_allowed() throws Exception {
        UUID userB = UUID.randomUUID();
        UUID catB = UUID.fromString(json(mockMvc.perform(post("/api/categories")
                        .with(asUser(userB))
                        .contentType(APPLICATION_JSON)
                        .content(categoryBody("Comida", "EXPENSE")))
                .andExpect(status().isCreated())
                .andReturn().getResponse()).get("id").asText());
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        createBudget(userId, cat.toString(), 2026, 2, "100.00");

        mockMvc.perform(post("/api/budgets")
                        .with(asUser(userB))
                        .contentType(APPLICATION_JSON)
                        .content(budgetBody(catB.toString(), 2026, 2, "100.00")))
                .andExpect(status().isCreated());
    }

    @Test
    void list_returnsBudgets() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        createBudget(userId, cat.toString(), 2026, 2, "100.00");
        createBudget(userId, cat.toString(), 2026, 3, "200.00");

        JsonNode page = json(mockMvc.perform(get("/api/budgets").with(asUser(userId)))
                .andExpect(status().isOk()).andReturn().getResponse());

        assertThat(page.get("content")).hasSize(2);
    }

    @Test
    void get_otherUser_returns404() throws Exception {
        UUID userB = UUID.randomUUID();
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        UUID budgetId = UUID.fromString(json(createBudget(userId, cat.toString(), 2026, 2, "100.00")).get("id").asText());

        mockMvc.perform(get("/api/budgets/" + budgetId).with(asUser(userB)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_returns200() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        UUID budgetId = UUID.fromString(json(createBudget(userId, cat.toString(), 2026, 2, "100.00")).get("id").asText());

        MockHttpServletResponse response = mockMvc.perform(put("/api/budgets/" + budgetId)
                        .with(asUser(userId))
                        .contentType(APPLICATION_JSON)
                        .content(budgetBody(cat.toString(), 2026, 2, "110.00")))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        JsonNode budget = json(response);
        assertThat(budget.get("amount").asText()).isEqualTo("110.0");
        assertThat(budget.get("year").asInt()).isEqualTo(2026);
        assertThat(budget.get("month").asInt()).isEqualTo(2);
    }

    @Test
    void delete_thenGet_returns404() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        UUID budgetId = UUID.fromString(json(createBudget(userId, cat.toString(), 2026, 2, "100.00")).get("id").asText());

        mockMvc.perform(delete("/api/budgets/" + budgetId).with(asUser(userId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/budgets/" + budgetId).with(asUser(userId)))
                .andExpect(status().isNotFound());
    }
}