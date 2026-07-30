package com.example.shipping.model;

import java.math.BigDecimal;

public enum DistanceZone {

    DOMESTIC(new BigDecimal("1.0")),
    EUROPEAN(new BigDecimal("1.5")),
    INTERNATIONAL(new BigDecimal("2.5"));

    private final BigDecimal multiplier;

    DistanceZone(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal multiplier() {
        return multiplier;
    }
}
