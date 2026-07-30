package com.example.shipping.controller;

import com.example.shipping.model.ShippingCost;
import com.example.shipping.model.ShippingRequest;
import com.example.shipping.service.ShippingCostService;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShippingController {

    private final ShippingCostService shippingCostService;

    public ShippingController(ShippingCostService shippingCostService) {
        this.shippingCostService = shippingCostService;
    }

    @PostMapping("/api/shipping/calculate")
    public ShippingCost calculate(@RequestBody ShippingRequest request) {
        BigDecimal baseRate = shippingCostService.baseRateFor(request.weightKg());
        BigDecimal zonedRate = shippingCostService.zonedRateFor(baseRate, request.zone());
        BigDecimal totalCost = shippingCostService.totalCostFor(zonedRate, request.zone(), request.orderTotal());
        boolean freeShippingApplied =
                shippingCostService.qualifiesForFreeShipping(request.zone(), request.orderTotal());
        return new ShippingCost(totalCost, new ShippingCost.Breakdown(baseRate, zonedRate, freeShippingApplied));
    }
}
