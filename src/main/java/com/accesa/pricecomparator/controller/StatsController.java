package com.accesa.pricecomparator.controller;


import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.TreeMap;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Statistics", description = "Pricing trends and store-level analysis")
@RequestMapping("/api/stats")
public class StatsController {

    private final ProductRepositoryInMemory productRepo;

    public StatsController(ProductRepositoryInMemory productRepo) {
        this.productRepo = productRepo;
    }


    @Operation(summary = "Show price trend for a given category in a store over time")
    @GetMapping("/category-price-trend")
    public Map<LocalDate, Double> getCategoryPriceTrend(@RequestParam String category,
                                                        @RequestParam String store) {
        return productRepo.getAll().stream()
                .filter(p -> p.getProductCategory().equalsIgnoreCase(category))
                .filter(p -> p.getStore().equalsIgnoreCase(store))
                .collect(Collectors.groupingBy(
                        Product::getDate,
                        TreeMap::new,
                        Collectors.averagingDouble(Product::getPrice)
                ));
    }


    @Operation(summary = "Get average product price per day in a specific store")
    @GetMapping("/store-daily-index")
    public Map<LocalDate, Double> getStoreIndex(@RequestParam String store) {
        return productRepo.getByStore(store).stream()
                .collect(Collectors.groupingBy(
                        Product::getDate,
                        TreeMap::new,
                        Collectors.averagingDouble(Product::getPrice)
                ));
    }


}
