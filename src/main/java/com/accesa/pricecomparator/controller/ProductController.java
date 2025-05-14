package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.util.CsvProductLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CsvProductLoader csvLoader;

    public ProductController(CsvProductLoader csvLoader) {
        this.csvLoader = csvLoader;
    }

    @GetMapping("/from-csv")
    public List<Product> getProductsFromCsv() {
        return csvLoader.loadProductsFromCsv("lidl_2025-05-08.csv", "Lidl", LocalDate.of(2025, 5, 8));
    }

    @GetMapping("/sample")
    public Product getSampleProduct() {
        return new Product(
                "P001",
                "lapte zuzu",
                "lactate",
                "Zuzu",
                1.0,
                "l",
                9.90,
                "RON",
                LocalDate.of(2025, 5, 8),
                "Lidl"
        );
    }
}
