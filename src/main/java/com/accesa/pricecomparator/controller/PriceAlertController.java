package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.model.PriceAlert;
import com.accesa.pricecomparator.service.PriceAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Slf4j
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
        log.info("Create a new price alert for a product");
        alertService.saveAlert(alert);
        return "Alert saved for " + alert.getProductName();
    }


    @Operation(summary = "Return all triggered alerts for a given date")
    @GetMapping("/triggered")
    public List<PriceAlert> getTriggeredAlerts(@RequestParam String date) {
        log.info("Get triggered alerts for a given date");
        return alertService.getTriggeredAlerts(date);
    }

}
