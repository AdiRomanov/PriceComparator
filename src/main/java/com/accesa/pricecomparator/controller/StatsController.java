package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.Map;



@RestController
@Tag(name = "Statistics", description = "Pricing trends and store-level analysis")
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }



    @Operation(summary = "Show price trend for a given category in a store over time")
    @GetMapping("/category-price-trend")
    public Map<LocalDate, Double> getCategoryPriceTrend(@RequestParam String category,
                                                        @RequestParam String store) {
        return statsService.getCategoryPriceTrend(category, store);
    }



    @Operation(summary = "Get average product price per day in a specific store")
    @GetMapping("/store-daily-index")
    public Map<LocalDate, Double> getStoreIndex(@RequestParam String store) {
        return statsService.getStoreDailyIndex(store);
    }


}
