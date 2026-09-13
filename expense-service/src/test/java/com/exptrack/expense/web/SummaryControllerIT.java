package com.exptrack.expense.web;

import com.exptrack.expense.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SummaryControllerIT extends AbstractIntegrationTest {

    private static final String MONTHLY_URL = "/api/summary/monthly?year=2026&month=2";

    @Test
    void monthly_aggregatesTotals() throws Exception {
        UUID comida = extractIdGivenCategory("Comida", "EXPENSE");
        UUID salario = extractIdGivenCategory("Salario", "INCOME");
        createBudget(userId, comida.toString(), 2026, 2, "100.00");
        createTransaction(userId, "EXPENSE", "120.00", comida.toString(), "2026-02-10", null);
        createTransaction(userId, "INCOME", "250.00", salario.toString(), "2026-02-10", null);

        JsonNode summary = json(mockMvc.perform(get(MONTHLY_URL).with(asUser(userId)))
                .andExpect(status().isOk()).andReturn().getResponse());

        assertThat(summary.get("year").asInt()).isEqualTo(2026);
        assertThat(summary.get("month").asInt()).isEqualTo(2);
        assertThat(summary.get("totalIncome").asText()).isEqualTo("250.0");
        assertThat(summary.get("totalExpense").asText()).isEqualTo("120.0");
        assertThat(summary.get("balance").asText()).isEqualTo("130.0");
        assertThat(summary.get("byCategory")).hasSize(2);
    }

    @Test
    void monthly_marksExceededBudget() throws Exception {
        UUID comida = extractIdGivenCategory("Comida", "EXPENSE");
        createBudget(userId, comida.toString(), 2026, 2, "100.00");
        createTransaction(userId, "EXPENSE", "120.00", comida.toString(), "2026-02-10", null);

        JsonNode summary = json(mockMvc.perform(get(MONTHLY_URL).with(asUser(userId)))
                .andExpect(status().isOk()).andReturn().getResponse());

        JsonNode comidaSummary = categoryNamed(summary, "Comida");
        assertThat(comidaSummary.get("budgetStatus").asText()).isEqualTo("EXCEEDED");
        assertThat(comidaSummary.get("budgetAmount").asText()).isEqualTo("100.0");
        assertThat(comidaSummary.get("type").asText()).isEqualTo("EXPENSE");
        assertThat(comidaSummary.get("amount").asText()).isEqualTo("120.0");
    }

    @Test
    void monthly_marksNoBudget() throws Exception {
        UUID salario = extractIdGivenCategory("Salario", "INCOME");
        createTransaction(userId, "INCOME", "250.00", salario.toString(), "2026-02-10", null);

        JsonNode summary = json(mockMvc.perform(get(MONTHLY_URL).with(asUser(userId)))
                .andExpect(status().isOk()).andReturn().getResponse());

        JsonNode salarioSummary = categoryNamed(summary, "Salario");
        assertThat(salarioSummary.get("budgetStatus").asText()).isEqualTo("NO_BUDGET");
        assertThat(salarioSummary.get("budgetAmount").isNull()).isTrue();
        assertThat(summary.get("totalIncome").asText()).isEqualTo("250.0");
    }

    @Test
    void monthly_withinLimit_marksWithinLimit() throws Exception {
        UUID comida = extractIdGivenCategory("Comida", "EXPENSE");
        createBudget(userId, comida.toString(), 2026, 2, "100.00");
        createTransaction(userId, "EXPENSE", "80.00", comida.toString(), "2026-02-10", null);

        JsonNode summary = json(mockMvc.perform(get(MONTHLY_URL).with(asUser(userId)))
                .andExpect(status().isOk()).andReturn().getResponse());

        assertThat(categoryNamed(summary, "Comida").get("budgetStatus").asText()).isEqualTo("WITHIN_LIMIT");
    }

    @Test
    void monthly_noData_returnsZeroTotals() throws Exception {
        JsonNode summary = json(mockMvc.perform(get(MONTHLY_URL).with(asUser(userId)))
                .andExpect(status().isOk()).andReturn().getResponse());

        assertThat(summary.get("totalIncome").asText()).isEqualTo("0");
        assertThat(summary.get("totalExpense").asText()).isEqualTo("0");
        assertThat(summary.get("balance").asText()).isEqualTo("0");
        assertThat(summary.get("byCategory")).isEmpty();
    }

    @Test
    void monthly_otherMonth_returnsZeroTotals() throws Exception {
        UUID comida = extractIdGivenCategory("Comida", "EXPENSE");
        createTransaction(userId, "EXPENSE", "120.00", comida.toString(), "2026-02-10", null);

        JsonNode summary = json(mockMvc.perform(get("/api/summary/monthly?year=2026&month=3").with(asUser(userId)))
                .andExpect(status().isOk()).andReturn().getResponse());

        assertThat(summary.get("totalExpense").asText()).isEqualTo("0");
        assertThat(summary.get("byCategory")).isEmpty();
    }

    @Test
    void monthly_missingMonth_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/summary/monthly?year=2026").with(asUser(userId)))
                .andExpect(status().isBadRequest());
    }

    private static JsonNode categoryNamed(JsonNode summary, String name) {
        for (JsonNode category : summary.get("byCategory")) {
            if (name.equals(category.get("categoryName").asText())) {
                return category;
            }
        }
        throw new AssertionError("Category not found in summary: " + name);
    }
}