package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.dto.*;
import com.accesa.pricecomparator.exception.ResourceNotFoundException;
import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
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

    private final ProductRepositoryInMemory productRepo;
    private final PriceComparatorService comparatorService;
    private final DiscountRepositoryInMemory discountRepo;


    public BasketController(ProductRepositoryInMemory productRepo,
                             PriceComparatorService comparatorService, DiscountRepositoryInMemory discountRepo) {
        this.productRepo = productRepo;
        this.comparatorService = comparatorService;
        this.discountRepo = discountRepo;
    }

    // POST /api/products/basket/optimize
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
        LocalDate date;
        try {
            date = LocalDate.parse(request.getDate());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Invalid date format. Use yyyy-MM-dd.");
        }
        List<Product> all = productRepo.getAll();
        List<Discount> discounts = discountRepo.getAll();

        List<BasketInvoiceItem> items = new ArrayList<>();
        double total = 0;
        double saved = 0;

        for (String name : request.getProductNames()) {
            Optional<Product> cheapest = all.stream()
                    .filter(p -> p.getProductName().equalsIgnoreCase(name) && p.getDate().isEqual(date))
                    .map(p -> {
                        double finalPrice = p.getPrice();
                        Discount discount = discounts.stream()
                                .filter(d -> d.getProductName().equalsIgnoreCase(p.getProductName()) &&
                                        d.getStore().equalsIgnoreCase(p.getStore()) &&
                                        !date.isBefore(d.getFromDate()) && !date.isAfter(d.getToDate()))
                                .findFirst().orElse(null);

                        if (discount != null) {
                            finalPrice -= finalPrice * discount.getPercentageOfDiscount() / 100.0;
                        }

                        return new Product(
                                p.getProductId(), p.getProductName(), p.getProductCategory(), p.getBrand(),
                                p.getPackageQuantity(), p.getPackageUnit(), finalPrice,
                                p.getCurrency(), p.getDate(), p.getStore()
                        );
                    })
                    .min(Comparator.comparingDouble(Product::getPrice));

            if (cheapest.isPresent()) {
                Product p = cheapest.get();
                double orig = all.stream()
                        .filter(prod -> prod.getProductName().equalsIgnoreCase(name) && prod.getStore().equalsIgnoreCase(p.getStore()) && prod.getDate().isEqual(date))
                        .mapToDouble(Product::getPrice)
                        .findFirst().orElse(p.getPrice());
                double diff = orig - p.getPrice();
                total += p.getPrice();
                saved += diff;

                items.add(new BasketInvoiceItem(p.getProductName(), p.getStore(), orig, p.getPrice(), diff));
            }
        }

        return new BasketInvoiceResponse(items, total, saved);
    }

    @Operation(summary = "Select best products within a limited budget",
            description = "Returns the most affordable products from given categories within the specified budget")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Budget basket calculated")
    })
    @PostMapping("/by-budget")
    public BasketResponse getWithinBudget(@RequestBody BudgetRequest request) {
        LocalDate date = request.getDate();
        double budget = request.getMaxBudget();

        List<Product> available = productRepo.getAll().stream()
                .filter(p -> p.getDate().isEqual(date))
                .filter(p -> request.getCategories().contains(p.getProductCategory()))
                .sorted(Comparator.comparingDouble(Product::getPrice))
                .toList();

        List<Product> selected = new ArrayList<>();
        double total = 0;

        for (Product p : available) {
            if (total + p.getPrice() <= budget) {
                selected.add(p);
                total += p.getPrice();
            }
        }

        List<BasketItemResponse> items = selected.stream()
                .map(p -> new BasketItemResponse(p.getProductName(), p.getStore(), p.getPrice()))
                .toList();

        return new BasketResponse(items, total, List.of());
    }


    @Operation(summary = "Compare basket prices across multiple days",
            description = "Calculates the total cost of a fixed basket on different days")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comparison successful")
    })
    @PostMapping("/compare-days")
    public Map<LocalDate, Double> compareBasketAcrossDays(@RequestBody BasketComparisonRequest req) {
        List<Product> all = productRepo.getAll();
        List<Discount> discounts = discountRepo.getAll();

        Map<LocalDate, Double> result = new TreeMap<>();

        for (LocalDate date : req.getDates()) {
            double total = 0;

            for (String name : req.getProductNames()) {
                Optional<Product> cheapest = all.stream()
                        .filter(p -> p.getProductName().equalsIgnoreCase(name))
                        .filter(p -> p.getDate().isEqual(date))
                        .map(p -> {
                            double price = p.getPrice();
                            Discount d = discounts.stream()
                                    .filter(dis -> dis.getProductName().equalsIgnoreCase(p.getProductName()) &&
                                            dis.getStore().equalsIgnoreCase(p.getStore()) &&
                                            !date.isBefore(dis.getFromDate()) && !date.isAfter(dis.getToDate()))
                                    .findFirst().orElse(null);
                            if (d != null)
                                price -= price * d.getPercentageOfDiscount() / 100.0;
                            return new Product(p.getProductId(), p.getProductName(), p.getProductCategory(),
                                    p.getBrand(), p.getPackageQuantity(), p.getPackageUnit(),
                                    price, p.getCurrency(), p.getDate(), p.getStore());
                        })
                        .min(Comparator.comparingDouble(Product::getPrice));

                total += cheapest.map(Product::getPrice).orElse(0.0);
            }

            result.put(date, total);
        }

        return result;
    }



}