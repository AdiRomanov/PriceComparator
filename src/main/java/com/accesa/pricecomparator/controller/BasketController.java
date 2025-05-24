package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.dto.*;
import com.accesa.pricecomparator.exception.ResourceNotFoundException;
import com.accesa.pricecomparator.service.BasketService;
import com.accesa.pricecomparator.service.PriceComparatorService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/basket")
@Tag(name = "Basket", description = "Endpoints for basket optimization, pricing, and comparisons")
public class BasketController {

    private final PriceComparatorService comparatorService;

    private final BasketService basketService;


    public BasketController(PriceComparatorService comparatorService,
                            BasketService basketService) {
        this.comparatorService = comparatorService;
        this.basketService = basketService;
    }

    @Operation(summary = "Optimize basket by selecting the cheapest products with discounts applied")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Basket optimized successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input format")
    })
    @PostMapping("/optimize")
    public BasketResponse optimizeBasket(@RequestBody BasketRequest request) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(request.getDate());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Invalid date format. Use yyyy-MM-dd.");
        }

        return comparatorService.optimizeBasket(request.getProductNames(), parsedDate);
    }



    @Operation(summary = "Get a detailed invoice for the selected products",
               description = "Returns original price, final price with discount, and total savings per product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invoice generated successfully")
    })
    @PostMapping("/invoice")
    public BasketInvoiceResponse getInvoice(@RequestBody BasketRequest request) {
        try {
            LocalDate.parse(request.getDate());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Invalid date format. Use yyyy-MM-dd.");
        }

        return basketService.getInvoice(request);
    }



    @Operation(summary = "Select best products within a limited budget",
            description = "Returns the most affordable products from given categories within the specified budget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget basket calculated")
    })
    @PostMapping("/by-budget")
    public BasketResponse getWithinBudget(@RequestBody BudgetRequest request) {
        return basketService.getWithinBudget(request);
    }




    @Operation(summary = "Compare basket prices across multiple days",
            description = "Calculates the total cost of a fixed basket on different days")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comparison successful")
    })
    @PostMapping("/compare-days")
    public Map<LocalDate, Double> compareBasketAcrossDays(@RequestBody BasketComparisonRequest req) {
        return basketService.compareBasketAcrossDays(req);
    }



}