package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.model.PriceAlert;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.repository.PriceAlertRepositoryInMemory;
import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
import com.accesa.pricecomparator.model.Product;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(name = "Price Alerts", description = "Create and track triggered price alerts")
@RequestMapping("/api/alerts")
public class PriceAlertController {

    private final PriceAlertRepositoryInMemory alertRepo;
    private final ProductRepositoryInMemory productRepo;
    private final DiscountRepositoryInMemory discountRepo;

    public PriceAlertController(PriceAlertRepositoryInMemory alertRepo,
                                ProductRepositoryInMemory productRepo,
                                DiscountRepositoryInMemory discountRepo) {
        this.alertRepo = alertRepo;
        this.productRepo = productRepo;
        this.discountRepo = discountRepo;
    }

    @Operation(summary = "Create a new price alert for a product")
    @PostMapping
    public String createAlert(@RequestBody PriceAlert alert) {
        alertRepo.addAlert(alert);
        return "Alert saved for " + alert.getProductName();
    }

    @Operation(summary = "Return all triggered alerts for a given date")
    @GetMapping("/triggered")
    public List<PriceAlert> getTriggeredAlerts(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        List<Product> products = productRepo.getAll().stream()
                .filter(p -> p.getDate().isEqual(parsedDate))
                .toList();

        List<Discount> discounts = discountRepo.getAll().stream()
                .filter(d -> !parsedDate.isBefore(d.getFromDate()) && !parsedDate.isAfter(d.getToDate()))
                .toList();

        return alertRepo.getAll().stream()
                .filter(alert -> products.stream().anyMatch(p -> {
                    if (!p.getProductName().equalsIgnoreCase(alert.getProductName())) return false;

                    double finalPrice = p.getPrice();
                    for (Discount d : discounts) {
                        if (d.getProductName().equalsIgnoreCase(p.getProductName()) &&
                                d.getStore().equalsIgnoreCase(p.getStore())) {
                            finalPrice -= finalPrice * d.getPercentageOfDiscount() / 100.0;
                            break;
                        }
                    }

                    return finalPrice <= alert.getTargetPrice();
                }))
                .toList();
    }

}
