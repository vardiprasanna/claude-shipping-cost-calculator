package com.example.shipping.acceptance;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
@DisplayName("Weight Tiers")
class WeightTiersAcceptanceIT {

    @Autowired
    private MockMvcTester mvc;

    @Nested
    @DisplayName("Must apply a flat base rate according to which weight tier a parcel falls into")
    class FlatBaseRatePerWeightTier {

        @ParameterizedTest(name = "The one where a {0}kg parcel is charged a ${1} base rate")
        @CsvSource({
                "0.999,  2.99",
                "1.000,  8.99",
                "20.000, 8.99",
        })
        void appliesFlatBaseRateForWeightTier(String weightKg, String expectedBaseRate) {
            MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": %s, "zone": "DOMESTIC", "orderTotal": 10.00 }
                            """.formatted(weightKg))
                    .exchange();

            assertThat(result).hasStatusOk();
            assertThat(result).bodyJson()
                    .extractingPath("$.breakdown.baseRate")
                    .convertTo(InstanceOfAssertFactories.BIG_DECIMAL)
                    .isEqualByComparingTo(expectedBaseRate);
        }
    }

    @Nested
    @DisplayName("Must add a $0.50/kg surcharge on top of the $8.99 base rate for the portion of weight strictly over 20kg")
    class SurchargeForWeightOverTwentyKg {

        @Test
        @DisplayName("The one where a parcel weighs 25kg => $8.99 + ($0.50 x 5kg) = $11.49")
        void surchargeAppliesToPortionOfWeightOverTwentyKg() {
            MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": 25.000, "zone": "DOMESTIC", "orderTotal": 10.00 }
                            """)
                    .exchange();

            assertThat(result).hasStatusOk();
            assertThat(result).bodyJson()
                    .extractingPath("$.breakdown.baseRate")
                    .convertTo(InstanceOfAssertFactories.BIG_DECIMAL)
                    .isEqualByComparingTo("11.49");
        }

        @Test
        @DisplayName("The one where a parcel weighs exactly 20.000kg => no surcharge applies, total is $8.99 (counter-example)")
        void noSurchargeAtExactlyTwentyKilograms() {
            MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": 20.000, "zone": "DOMESTIC", "orderTotal": 10.00 }
                            """)
                    .exchange();

            assertThat(result).hasStatusOk();
            assertThat(result).bodyJson()
                    .extractingPath("$.breakdown.baseRate")
                    .convertTo(InstanceOfAssertFactories.BIG_DECIMAL)
                    .isEqualByComparingTo("8.99");
        }
    }

    @Nested
    @DisplayName("Must reject a shipment with zero or negative weight as a 400 Bad Request")
    class RejectsZeroOrNegativeWeight {

        @Test
        @DisplayName("The one where weight is -2kg => 400 Bad Request, no cost calculated")
        void negativeWeightIsRejectedWithBadRequest() {
            MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": -2.000, "zone": "DOMESTIC", "orderTotal": 10.00 }
                            """)
                    .exchange();

            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("The one where weight is 0.001kg (smallest valid positive weight) => accepted, priced at the under-1kg tier, $2.99 (counter-example)")
        void smallestValidPositiveWeightIsAccepted() {
            MvcTestResult result = mvc.post().uri("/api/shipping/calculate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "weightKg": 0.001, "zone": "DOMESTIC", "orderTotal": 10.00 }
                            """)
                    .exchange();

            assertThat(result).hasStatusOk();
            assertThat(result).bodyJson()
                    .extractingPath("$.breakdown.baseRate")
                    .convertTo(InstanceOfAssertFactories.BIG_DECIMAL)
                    .isEqualByComparingTo("2.99");
        }
    }
}
