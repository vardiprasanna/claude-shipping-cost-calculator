package com.example.shipping.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ShippingCostServiceTest {

    private final ShippingCostService service = new ShippingCostService();

    @ParameterizedTest(name = "The one where a {0}kg parcel is charged a ${1} base rate")
    @CsvSource({
            "0.999,  2.99",
            "1.000,  8.99",
            "25.000, 11.49",
    })
    void appliesFlatBaseRateForWeightTier(String weightKg, String expectedBaseRate) {
        assertThat(service.baseRateFor(new BigDecimal(weightKg)))
                .isEqualByComparingTo(expectedBaseRate);
    }

    @Test
    @DisplayName("The one where a -2kg parcel is rejected as invalid weight")
    void negativeWeightIsRejected() {
        assertThatThrownBy(() -> service.baseRateFor(new BigDecimal("-2.000")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
