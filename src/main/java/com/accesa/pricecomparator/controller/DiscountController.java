package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.util.CsvDiscountLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private final CsvDiscountLoader discountLoader;

    public DiscountController(CsvDiscountLoader discountLoader) {
        this.discountLoader = discountLoader;
    }

    @GetMapping("/from-csv")
    public List<Discount> getDiscountsFromCsv() {
        return discountLoader.loadDiscountsFromCsv("lidl_discounts_2025-05-08.csv", "Lidl");
    }
}
