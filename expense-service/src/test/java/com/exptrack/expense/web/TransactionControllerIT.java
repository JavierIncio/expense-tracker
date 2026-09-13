package com.exptrack.expense.web;

import com.exptrack.expense.AbstractIntegrationTest;
import com.exptrack.expense.events.BudgetExceededEvent;
import com.exptrack.expense.events.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerIT extends AbstractIntegrationTest {

    @MockitoBean
    DomainEventPublisher eventPublisher;

    @Test
    void create_returns201_withDescription() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");

        MockHttpServletResponse response = mockMvc.perform(post("/api/transactions")
                        .with(asUser(userId))
                        .contentType(APPLICATION_JSON)
                        .content(transactionBody("EXPENSE", "50.00", cat.toString(), "2026-02-10", "almuerzo")))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        JsonNode tx = json(response);
        assertThat(tx.get("type").asText()).isEqualTo("EXPENSE");
        assertThat(tx.get("description").asText()).isEqualTo("almuerzo");
        assertThat(tx.get("amount").asText()).isEqualTo("50.0");
        assertThat(tx.get("categoryId").asText()).isEqualTo(cat.toString());
        assertThat(tx.get("date").asText()).isEqualTo("2026-02-10");
    }

    @Test
    void create_withoutDescription_returnsNullDescription() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");

        MockHttpServletResponse response = mockMvc.perform(post("/api/transactions")
                        .with(asUser(userId))
                        .contentType(APPLICATION_JSON)
                        .content(transactionBody("EXPENSE", "10.00", cat.toString(), "2026-02-10", null)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        assertThat(json(response).get("description").isNull()).isTrue();
    }

    @Test
    void create_typeMismatch_returns400() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");

        mockMvc.perform(post("/api/transactions")
                        .with(asUser(userId))
                        .contentType(APPLICATION_JSON)
                        .content(transactionBody("INCOME", "50.00", cat.toString(), "2026-02-10", null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_unknownCategory_returns404() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .with(asUser(userId))
                        .contentType(APPLICATION_JSON)
                        .content(transactionBody("EXPENSE", "50.00", UUID.randomUUID().toString(), "2026-02-10", null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_expenseOverBudget_publishesEvent() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        createBudget(userId, cat.toString(), 2026, 2, "100.00");

        createTransaction(userId, "EXPENSE", "120.00", cat.toString(), "2026-02-10", null);

        verify(eventPublisher).publishBudgetExceeded(any(BudgetExceededEvent.class));
    }

    @Test
    void create_expenseWithinBudget_doesNotPublish() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        createBudget(userId, cat.toString(), 2026, 2, "100.00");

        createTransaction(userId, "EXPENSE", "80.00", cat.toString(), "2026-02-10", null);

        verify(eventPublisher, never()).publishBudgetExceeded(any(BudgetExceededEvent.class));
    }

    @Test
    void get_otherUser_returns404() throws Exception {
        UUID userB = UUID.randomUUID();
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        UUID txId = UUID.fromString(json(createTransaction(userId, "EXPENSE", "10.00", cat.toString(), "2026-02-10", null)).get("id").asText());

        mockMvc.perform(get("/api/transactions/" + txId).with(asUser(userB)))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_defaultSortsByDateDesc() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        createTransaction(userId, "EXPENSE", "50.00", cat.toString(), "2026-02-01", null);
        createTransaction(userId, "EXPENSE", "80.00", cat.toString(), "2026-02-10", null);

        JsonNode page = json(mockMvc.perform(get("/api/transactions").with(asUser(userId)))
                .andExpect(status().isOk()).andReturn().getResponse());

        assertThat(page.get("content")).hasSize(2);
        assertThat(page.get("content").get(0).get("date").asText()).isEqualTo("2026-02-10");
        assertThat(page.get("content").get(1).get("date").asText()).isEqualTo("2026-02-01");
    }

    @Test
    void list_sortableByAmountAsc() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        createTransaction(userId, "EXPENSE", "80.00", cat.toString(), "2026-02-10", null);
        createTransaction(userId, "EXPENSE", "50.00", cat.toString(), "2026-02-01", null);

        JsonNode page = json(mockMvc.perform(get("/api/transactions?sort=amount,asc").with(asUser(userId)))
                .andExpect(status().isOk()).andReturn().getResponse());

        assertThat(page.get("content").get(0).get("amount").asText()).isEqualTo("50.0");
        assertThat(page.get("content").get(1).get("amount").asText()).isEqualTo("80.0");
    }

    @Test
    void update_returns200() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        UUID txId = UUID.fromString(json(createTransaction(userId, "EXPENSE", "50.00", cat.toString(), "2026-02-10", "v1")).get("id").asText());

        MockHttpServletResponse response = mockMvc.perform(put("/api/transactions/" + txId)
                        .with(asUser(userId))
                        .contentType(APPLICATION_JSON)
                        .content(transactionBody("EXPENSE", "90.00", cat.toString(), "2026-02-11", "v2")))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        JsonNode tx = json(response);
        assertThat(tx.get("amount").asText()).isEqualTo("90.0");
        assertThat(tx.get("date").asText()).isEqualTo("2026-02-11");
        assertThat(tx.get("description").asText()).isEqualTo("v2");
    }

    @Test
    void delete_thenGet_returns404() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        UUID txId = UUID.fromString(json(createTransaction(userId, "EXPENSE", "50.00", cat.toString(), "2026-02-10", null)).get("id").asText());

        mockMvc.perform(delete("/api/transactions/" + txId).with(asUser(userId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/transactions/" + txId).with(asUser(userId)))
                .andExpect(status().isNotFound());
    }
}