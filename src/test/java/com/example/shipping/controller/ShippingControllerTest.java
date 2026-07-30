package com.example.shipping.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.shipping.model.DistanceZone;
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
import org.springframework.test.web.servlet.assertj.MvcTestResult;

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

    @Test
    @DisplayName("The one where an unrecognized zone value is mapped to 400 Bad Request")
    void mapsUnrecognizedZoneToBadRequest() {
        assertThat(mvc.post().uri("/api/shipping/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "weightKg": 1.000, "zone": "MARS", "orderTotal": 10.00 }
                        """))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("The one where the endpoint applies the zone multiplier and returns the zoned rate as JSON")
    void returnsServiceZonedRateAsJson() {
        given(shippingCostService.baseRateFor(any(BigDecimal.class))).willReturn(new BigDecimal("8.99"));
        given(shippingCostService.zonedRateFor(any(BigDecimal.class), any(DistanceZone.class)))
                .willReturn(new BigDecimal("13.49"));

        MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "weightKg": 1.000, "zone": "EUROPEAN", "orderTotal": 10.00 }
                        """)
                .exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson()
                .extractingPath("$.breakdown.zonedRate")
                .convertTo(InstanceOfAssertFactories.BIG_DECIMAL)
                .isEqualByComparingTo("13.49");
    }

    @Test
    @DisplayName("The one where the controller returns the service's totalCost as JSON")
    void returnsServiceTotalCostAsJson() {
        given(shippingCostService.baseRateFor(any(BigDecimal.class))).willReturn(new BigDecimal("8.99"));
        given(shippingCostService.zonedRateFor(any(BigDecimal.class), any(DistanceZone.class)))
                .willReturn(new BigDecimal("13.49"));
        given(shippingCostService.totalCostFor(any(BigDecimal.class), any(DistanceZone.class), any(BigDecimal.class)))
                .willReturn(new BigDecimal("0.00"));

        MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "weightKg": 1.000, "zone": "DOMESTIC", "orderTotal": 100.00 }
                        """)
                .exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson()
                .extractingPath("$.totalCost")
                .convertTo(InstanceOfAssertFactories.BIG_DECIMAL)
                .isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("The one where the endpoint reports freeShippingApplied as true in the breakdown")
    void reportsFreeShippingAppliedInBreakdown() {
        given(shippingCostService.baseRateFor(any(BigDecimal.class))).willReturn(new BigDecimal("8.99"));
        given(shippingCostService.zonedRateFor(any(BigDecimal.class), any(DistanceZone.class)))
                .willReturn(new BigDecimal("8.99"));
        given(shippingCostService.totalCostFor(any(BigDecimal.class), any(DistanceZone.class), any(BigDecimal.class)))
                .willReturn(new BigDecimal("0.00"));
        given(shippingCostService.qualifiesForFreeShipping(any(DistanceZone.class), any(BigDecimal.class)))
                .willReturn(true);

        MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "weightKg": 1.000, "zone": "DOMESTIC", "orderTotal": 100.00 }
                        """)
                .exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson()
                .extractingPath("$.breakdown.freeShippingApplied")
                .asBoolean()
                .isTrue();
    }
}
