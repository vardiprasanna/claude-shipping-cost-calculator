package com.example.shipping.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    @DisplayName("The one where GET /api/admin/rates returns 200 OK")
    void ratesEndpointReturnsOk() {
        assertThat(mvc.get().uri("/api/admin/rates")).hasStatusOk();
    }
}
