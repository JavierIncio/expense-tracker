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

class CategoryControllerIT extends AbstractIntegrationTest {

    @Test
    void create_returns201() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/api/categories")
                        .with(asUser(userId))
                        .contentType(APPLICATION_JSON)
                        .content(categoryBody("Comida", "EXPENSE")))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        JsonNode category = json(response);
        assertThat(category.get("name").asText()).isEqualTo("Comida");
        assertThat(category.get("type").asText()).isEqualTo("EXPENSE");
        assertThat(category.get("id").asText()).isNotBlank();
    }

    @Test
    void create_missingName_returns400() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .with(asUser(userId))
                        .contentType(APPLICATION_JSON)
                        .content("{\"type\":\"EXPENSE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_returnsAll() throws Exception {
        extractIdGivenCategory("Comida", "EXPENSE");
        extractIdGivenCategory("Salario", "INCOME");

        JsonNode categories = json(mockMvc.perform(get("/api/categories").with(asUser(userId)))
                .andExpect(status().isOk()).andReturn().getResponse());

        assertThat(categories).hasSize(2);
    }

    @Test
    void list_filteredByType() throws Exception {
        extractIdGivenCategory("Comida", "EXPENSE");
        extractIdGivenCategory("Salario", "INCOME");

        JsonNode categories = json(mockMvc.perform(get("/api/categories?type=EXPENSE").with(asUser(userId)))
                .andExpect(status().isOk()).andReturn().getResponse());

        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).get("name").asText()).isEqualTo("Comida");
    }

    @Test
    void get_returnsCategory() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");

        mockMvc.perform(get("/api/categories/" + cat).with(asUser(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Comida"))
                .andExpect(jsonPath("$.id").value(cat.toString()));
    }

    @Test
    void get_otherUser_returns404() throws Exception {
        UUID userB = UUID.randomUUID();
        UUID catB = UUID.fromString(json(mockMvc.perform(post("/api/categories")
                        .with(asUser(userB))
                        .contentType(APPLICATION_JSON)
                        .content(categoryBody("Comida", "EXPENSE")))
                .andExpect(status().isCreated())
                .andReturn().getResponse()).get("id").asText());

        mockMvc.perform(get("/api/categories/" + catB).with(asUser(userId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_returns200() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");

        mockMvc.perform(put("/api/categories/" + cat)
                        .with(asUser(userId))
                        .contentType(APPLICATION_JSON)
                        .content(categoryBody("Restaurante", "EXPENSE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Restaurante"));
    }

    @Test
    void delete_inUse_returns409() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");
        createTransaction(userId, "EXPENSE", "10.00", cat.toString(), "2026-02-10", null);

        mockMvc.perform(delete("/api/categories/" + cat).with(asUser(userId)))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/categories/" + cat).with(asUser(userId)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ok_thenGet_returns404() throws Exception {
        UUID cat = extractIdGivenCategory("Comida", "EXPENSE");

        mockMvc.perform(delete("/api/categories/" + cat).with(asUser(userId)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories/" + cat).with(asUser(userId)))
                .andExpect(status().isNotFound());
    }
}