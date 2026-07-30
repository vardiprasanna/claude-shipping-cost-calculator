package com.example.shipping.service;

import com.example.shipping.model.DistanceZone;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class ShippingCostService {

    private static final BigDecimal UNDER_ONE_KG_RATE = new BigDecimal("2.99");
    private static final BigDecimal ONE_TO_TWENTY_KG_RATE = new BigDecimal("8.99");
    private static final BigDecimal TWENTY_KG = new BigDecimal("20");
    private static final BigDecimal SURCHARGE_PER_KG_OVER_TWENTY = new BigDecimal("0.50");

    public BigDecimal baseRateFor(BigDecimal weightKg) {
        if (weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("weightKg must be positive");
        }
        if (weightKg.compareTo(BigDecimal.ONE) < 0) {
            return UNDER_ONE_KG_RATE;
        }
        if (weightKg.compareTo(TWENTY_KG) > 0) {
            BigDecimal kgOverTwenty = weightKg.subtract(TWENTY_KG);
            return ONE_TO_TWENTY_KG_RATE.add(SURCHARGE_PER_KG_OVER_TWENTY.multiply(kgOverTwenty))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return ONE_TO_TWENTY_KG_RATE;
    }

    public BigDecimal zonedRateFor(BigDecimal baseRate, DistanceZone zone) {
        return baseRate.multiply(zone.multiplier()).setScale(2, RoundingMode.HALF_UP);
    }
}
