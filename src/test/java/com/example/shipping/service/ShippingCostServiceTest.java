package com.example.shipping.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shipping.model.DistanceZone;
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

    @ParameterizedTest(name = "The one where a ${0} base rate in the {1} zone has a ${2} zoned rate")
    @CsvSource({
            "8.99, DOMESTIC,      8.99",
            "8.99, EUROPEAN,      13.49",
            "8.99, INTERNATIONAL, 22.48",
            "2.99, EUROPEAN,      4.49",
    })
    void appliesZoneMultiplierToBaseRate(String baseRate, DistanceZone zone, String expectedZonedRate) {
        assertThat(service.zonedRateFor(new BigDecimal(baseRate), zone))
                .isEqualByComparingTo(expectedZonedRate);
    }

    @ParameterizedTest(name = "The one where a {0} order totalling ${1} qualifies for free shipping: {2}")
    @CsvSource({
            "DOMESTIC,      74.99,  false",
            "DOMESTIC,      75.00,  true",
            "EUROPEAN,      100.00, false",
            "INTERNATIONAL, 100.00, false",
    })
    void ordersQualifyForFreeShippingOnlyWhenDomesticAndAtOrAbove75(
            DistanceZone zone, String orderTotal, boolean expectedQualifies) {
        assertThat(service.qualifiesForFreeShipping(zone, new BigDecimal(orderTotal)))
                .isEqualTo(expectedQualifies);
    }

    @ParameterizedTest(name = "The one where a ${0} zoned rate for a {1} order totalling ${2} has a ${3} totalCost")
    @CsvSource({
            "11.49, DOMESTIC, 100.00, 0.00",
            "11.49, DOMESTIC, 50.00,  11.49",
    })
    void totalCostIsZeroedOnlyWhenFreeShippingApplies(
            String zonedRate, DistanceZone zone, String orderTotal, String expectedTotalCost) {
        assertThat(service.totalCostFor(new BigDecimal(zonedRate), zone, new BigDecimal(orderTotal)))
                .isEqualByComparingTo(expectedTotalCost);
    }
}
