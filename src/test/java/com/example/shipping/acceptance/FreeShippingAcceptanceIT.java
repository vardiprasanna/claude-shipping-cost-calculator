package com.example.shipping.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Free Shipping")
class FreeShippingAcceptanceIT {

    @Autowired
    private MockMvcTester mvc;

    @Nested
    @DisplayName("Must waive the shipping cost entirely for a domestic order whose total is $75.00 or more")
    class WaivesShippingForQualifyingDomesticOrder {

        @ParameterizedTest(name = "The one where a {0} order totalling ${1} costs ${2} in shipping")
        @CsvSource({
                "DOMESTIC,      74.99,  8.99",
                "DOMESTIC,      75.00,  0.00",
                "DOMESTIC,      100.00, 0.00",
                "EUROPEAN,      100.00, 13.49",
                "INTERNATIONAL, 100.00, 22.48",
        })
        void domesticOrderAtOrAbove75GetsFreeShipping(String zone, String orderTotal, String expectedTotalCost) {
            MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                    .header("X-API-Key", "user-test-key")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": 1.000, "zone": "%s", "orderTotal": %s }
                            """.formatted(zone, orderTotal))
                    .exchange();

            assertThat(result).hasStatusOk();
            assertThat(result).bodyJson()
                    .extractingPath("$.totalCost")
                    .convertTo(InstanceOfAssertFactories.BIG_DECIMAL)
                    .isEqualByComparingTo(expectedTotalCost);
        }
    }

    @Nested
    @DisplayName("Must apply the free-shipping waiver after weight and zone charges are calculated, "
            + "overriding even a heavy, surcharged parcel's cost")
    class WaiverOverridesHeavySurchargedParcel {

        @Test
        @DisplayName("The one where a 25kg domestic parcel with a $100.00 order total has its $11.49 "
                + "pre-waiver cost reduced to $0.00")
        void heavyParcelWithQualifyingOrderGetsFreeShipping() {
            MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                    .header("X-API-Key", "user-test-key")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": 25.000, "zone": "DOMESTIC", "orderTotal": 100.00 }
                            """)
                    .exchange();

            assertThat(result).hasStatusOk();
            assertThat(result).bodyJson()
                    .extractingPath("$.totalCost")
                    .convertTo(InstanceOfAssertFactories.BIG_DECIMAL)
                    .isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("The one where the same 25kg domestic parcel with only a $50.00 order total "
                + "is charged the full $11.49, no waiver (counter-example)")
        void heavyParcelBelowThresholdIsChargedInFull() {
            MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                    .header("X-API-Key", "user-test-key")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": 25.000, "zone": "DOMESTIC", "orderTotal": 50.00 }
                            """)
                    .exchange();

            assertThat(result).hasStatusOk();
            assertThat(result).bodyJson()
                    .extractingPath("$.totalCost")
                    .convertTo(InstanceOfAssertFactories.BIG_DECIMAL)
                    .isEqualByComparingTo("11.49");
        }
    }
}
