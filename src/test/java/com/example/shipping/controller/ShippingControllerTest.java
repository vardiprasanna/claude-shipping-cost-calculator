package com.example.shipping.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.shipping.service.ShippingCostService;
import java.math.BigDecimal;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ShippingController.class)
class ShippingControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private ShippingCostService shippingCostService;

    @Test
    @DisplayName("The one where the endpoint returns the service's base rate as JSON")
    void returnsServiceBaseRateAsJson() {
        given(shippingCostService.baseRateFor(any(BigDecimal.class))).willReturn(new BigDecimal("2.99"));

        assertThat(mvc.post().uri("/api/shipping/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "weightKg": 0.999, "zone": "DOMESTIC", "orderTotal": 10.00 }
                        """))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.breakdown.baseRate")
                .convertTo(InstanceOfAssertFactories.BIG_DECIMAL)
                .isEqualByComparingTo("2.99");
    }

    @Test
    @DisplayName("The one where a rejected weight is mapped to 400 Bad Request")
    void mapsInvalidWeightToBadRequest() {
        given(shippingCostService.baseRateFor(any(BigDecimal.class)))
                .willThrow(new IllegalArgumentException("weightKg must be positive"));

        assertThat(mvc.post().uri("/api/shipping/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "weightKg": -2.000, "zone": "DOMESTIC", "orderTotal": 10.00 }
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
