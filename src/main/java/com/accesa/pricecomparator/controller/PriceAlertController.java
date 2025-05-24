package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.model.PriceAlert;
import com.accesa.pricecomparator.service.PriceAlertService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@Tag(name = "Price Alerts", description = "Create and track triggered price alerts")
@RequestMapping("/api/alerts")
public class PriceAlertController {

    private final PriceAlertService alertService;

    public PriceAlertController(PriceAlertService alertService) {
        this.alertService = alertService;
    }


    @Operation(summary = "Create a new price alert for a product")
    @PostMapping
    public String createAlert(@RequestBody PriceAlert alert) {
        alertService.saveAlert(alert);
        return "Alert saved for " + alert.getProductName();
    }


    @Operation(summary = "Return all triggered alerts for a given date")
    @GetMapping("/triggered")
    public List<PriceAlert> getTriggeredAlerts(@RequestParam String date) {
        return alertService.getTriggeredAlerts(date);
    }

}
