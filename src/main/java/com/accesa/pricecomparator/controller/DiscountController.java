package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.util.CsvDiscountLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private final CsvDiscountLoader discountLoader;
    private final DiscountRepositoryInMemory discountRepo;


    public DiscountController(CsvDiscountLoader discountLoader, DiscountRepositoryInMemory discountRepo) {
        this.discountLoader = discountLoader;
        this.discountRepo = discountRepo;
    }

    @GetMapping("/from-csv")
    public List<Discount> getDiscountsFromCsv() {
        return discountLoader.loadDiscountsFromCsv("lidl_discounts_2025-05-08.csv", "Lidl");
    }

    // GET /api/discounts/all
    @GetMapping("/all")
    public List<Discount> getAllDiscounts() {
        return discountRepo.getAll();
    }

    // GET /api/discounts/store/{store}
    @GetMapping("/store/{store}")
    public List<Discount> getDiscountsByStore(@PathVariable String store) {
        return discountRepo.getByStore(store.toLowerCase());
    }

    // GET /api/discounts/best
    @GetMapping("/best")
    public List<Discount> getBestActiveDiscounts() {
        return discountRepo.getActiveDiscounts().stream()
                .sorted((d1, d2) -> Integer.compare(d2.getPercentageOfDiscount(), d1.getPercentageOfDiscount()))
                .limit(10)
                .toList();
    }

}
