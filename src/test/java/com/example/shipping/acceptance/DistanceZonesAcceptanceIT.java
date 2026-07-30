package com.example.shipping.acceptance;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Distance Zones")
class DistanceZonesAcceptanceIT {

    @Autowired
    private MockMvcTester mvc;

    @Nested
    @DisplayName("Must apply a zone multiplier to the weight-tier base rate to produce the zoned rate")
    class ZoneMultiplierAppliedToBaseRate {

        @Test
        @DisplayName("The one where a European parcel with an $8.99 base rate has a $13.49 zoned rate")
        void appliesZoneMultiplierToBaseRate() {
            MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                    .header("X-API-Key", "user-test-key")
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
    }

    @Nested
    @DisplayName("Must reject a shipment with an unrecognized destination zone as a 400 Bad Request")
    class RejectsUnrecognizedZone {

        @Test
        @DisplayName("The one where zone is \"MARS\" => 400 Bad Request, no cost calculated")
        void unrecognizedZoneIsRejectedWithBadRequest() {
            MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                    .header("X-API-Key", "user-test-key")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": 1.000, "zone": "MARS", "orderTotal": 10.00 }
                            """)
                    .exchange();

            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("The one where zone is \"INTERNATIONAL\" (the least common but still valid zone) => accepted, x2.5 applied (counter-example)")
        void leastCommonValidZoneIsAccepted() {
            MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                    .header("X-API-Key", "user-test-key")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": 1.000, "zone": "INTERNATIONAL", "orderTotal": 10.00 }
                            """)
                    .exchange();

            assertThat(result).hasStatusOk();
            assertThat(result).bodyJson()
                    .extractingPath("$.breakdown.zonedRate")
                    .convertTo(InstanceOfAssertFactories.BIG_DECIMAL)
                    .isEqualByComparingTo("22.48");
        }
    }
}
