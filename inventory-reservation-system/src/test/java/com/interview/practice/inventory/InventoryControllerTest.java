package com.interview.practice.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Basic integration tests to verify boilerplate setup
 */
@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testStatusEndpoint() throws Exception {
        mockMvc.perform(get("/api/inventory/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Inventory Reservation System"));
    }

    @Test
    void testGetInventoryEndpoint() throws Exception {
        mockMvc.perform(get("/api/inventory/LAPTOP-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("LAPTOP-001"))
                .andExpect(jsonPath("$.availableQuantity").value(100));
    }

    @Test
    void testGetNonExistentInventory() throws Exception {
        mockMvc.perform(get("/api/inventory/INVALID-SKU"))
                .andExpect(status().isNotFound());
    }
}

