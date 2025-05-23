package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.exception.ResourceNotFoundException;
import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.util.CsvDiscountLoader;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(name = "Discounts", description = "Access and filter discounts per day, store, and product")
@RequestMapping("/api/discounts")
public class DiscountController {

    private final CsvDiscountLoader discountLoader;
    private final DiscountRepositoryInMemory discountRepo;


    public DiscountController(CsvDiscountLoader discountLoader, DiscountRepositoryInMemory discountRepo) {
        this.discountLoader = discountLoader;
        this.discountRepo = discountRepo;
    }


    @Operation(summary = "Load discounts from a sample CSV file")
    @GetMapping("/from-csv")
    public List<Discount> getDiscountsFromCsv() {
        return discountLoader.loadDiscountsFromCsv("lidl_discounts_2025-05-08.csv", "Lidl");
    }

    @Operation(summary = "List all loaded discounts")
    @GetMapping("/all")
    public List<Discount> getAllDiscounts() {
        return discountRepo.getAll();
    }

    @Operation(summary = "Get all discounts available in a specific store")
    @GetMapping("/store/{store}")
    public List<Discount> getDiscountsByStore(@PathVariable String store) {
        return discountRepo.getByStore(store.toLowerCase());
    }

    @Operation(summary = "Top 10 currently active discounts by percentage")
    @GetMapping("/best")
    public List<Discount> getBestActiveDiscounts() {
        return discountRepo.getActiveDiscounts().stream()
                .sorted((d1, d2) -> Integer.compare(d2.getPercentageOfDiscount(), d1.getPercentageOfDiscount()))
                .limit(10)
                .toList();
    }

    @Operation(summary = "Get discounts that started on a specific date")
    @GetMapping("/new")
    public List<Discount> getNewDiscounts(@RequestParam String date) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Invalid date format. Use yyyy-MM-dd.");
        }

        return discountRepo.getNewDiscounts(parsedDate);
    }


    @Operation(summary = "Get all discounts higher than a given percentage")
    @GetMapping("/above")
    public List<Discount> getBigDiscounts(@RequestParam double percent,
                                          @RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return discountRepo.getAll().stream()
                .filter(d -> !parsedDate.isBefore(d.getFromDate()) && !parsedDate.isAfter(d.getToDate()))
                .filter(d -> d.getPercentageOfDiscount() > percent)
                .toList();
    }


    @Operation(summary = "List all discounts expiring on a specific date")
    @GetMapping("/expiring")
    public List<Discount> getExpiringDiscounts(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return discountRepo.getAll().stream()
                .filter(d -> d.getToDate().isEqual(parsedDate))
                .toList();
    }


}
