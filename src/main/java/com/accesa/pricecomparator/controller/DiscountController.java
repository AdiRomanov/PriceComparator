package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.service.DiscountService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@Tag(name = "Discounts", description = "Access and filter discounts per day, store, and product")
@RequestMapping("/api/discounts")
public class DiscountController {

    private final DiscountService discountService;


    public DiscountController(DiscountService discountService) {

        this.discountService = discountService;
    }


    @Operation(summary = "Load discounts from a sample CSV file")
    @GetMapping("/from-csv")
    public List<Discount> getDiscountsFromCsv() {
        return discountService.loadFromCsv();
    }

    @Operation(summary = "List all loaded discounts")
    @GetMapping("/all")
    public List<Discount> getAllDiscounts() {
        return discountService.getAll();
    }

    @Operation(summary = "Get all discounts available in a specific store")
    @GetMapping("/store/{store}")
    public List<Discount> getDiscountsByStore(@PathVariable String store) {
        return discountService.getByStore(store);
    }

    @Operation(summary = "Top 10 currently active discounts by percentage")
    @GetMapping("/best")
    public List<Discount> getBestActiveDiscounts() {
        return discountService.getBestActive();
    }

    @Operation(summary = "Get discounts that started on a specific date")
    @GetMapping("/new")
    public List<Discount> getNewDiscounts(@RequestParam String date) {
        return discountService.getNewDiscounts(date);
    }


    @Operation(summary = "Get all discounts higher than a given percentage")
    @GetMapping("/above")
    public List<Discount> getBigDiscounts(@RequestParam double percent,
                                          @RequestParam String date) {
        return discountService.getAbovePercentage(date, percent);
    }


    @Operation(summary = "List all discounts expiring on a specific date")
    @GetMapping("/expiring")
    public List<Discount> getExpiringDiscounts(@RequestParam String date) {
        return discountService.getExpiring(date);
    }


}
