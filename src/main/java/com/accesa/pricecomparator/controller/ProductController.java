package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.exception.ResourceNotFoundException;
import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
import com.accesa.pricecomparator.service.PriceComparatorService;
import com.accesa.pricecomparator.util.CsvProductLoader;
import org.springframework.web.bind.annotation.*;
import com.accesa.pricecomparator.dto.*;


import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CsvProductLoader csvLoader;
    private final ProductRepositoryInMemory productRepo;
    private final PriceComparatorService comparatorService;



    public ProductController(CsvProductLoader csvLoader, ProductRepositoryInMemory productRepo,
                             PriceComparatorService comparatorService) {
        this.csvLoader = csvLoader;
        this.productRepo = productRepo;
        this.comparatorService = comparatorService;
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

    // GET /api/products/all
    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productRepo.getAll();
    }

    // GET /api/products/store/{store}
    @GetMapping("/store/{store}")
    public List<Product> getProductsByStore(@PathVariable String store) {
        return productRepo.getByStore(store.toLowerCase());
    }

    // GET /api/products/cheapest-by-name?name=vin%20alb%20demisec&date=2025-05-08
    @GetMapping("/cheapest-by-name")
    public Product getCheapestProductByNameAndDate(@RequestParam String name,
                                                   @RequestParam String date) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Invalid date format. Use yyyy-MM-dd.");
        }

        return comparatorService.findCheapestStoreForProductByName(name, parsedDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product '" + name + "' not found for date " + date));
    }

    // POST /api/products/basket/optimize
    @PostMapping("/basket/optimize")
    public BasketResponse optimizeBasket(@RequestBody BasketRequest request) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(request.getDate());
        } catch (Exception e) {
            throw new ResourceNotFoundException("Invalid date format. Use yyyy-MM-dd.");
        }

        return comparatorService.optimizeBasket(request.getProductNames(), parsedDate);
    }


    // GET /api/products/price-history?name=lapte%20zuzu
    @GetMapping("/price-history")
    public List<PriceHistoryEntry> getPriceHistory(@RequestParam String name) {
        List<PriceHistoryEntry> history = comparatorService.getPriceHistoryForProduct(name);
        if (history.isEmpty()) {
            throw new ResourceNotFoundException("No price history found for product: " + name);
        }
        return history;
    }

    // GET /api/products/substitutes?name=lapte%20zuzu&date=2025-05-08
    @GetMapping("/substitutes")
    public List<Product> getSubstitutes(@RequestParam String name, @RequestParam String date) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Invalid date format.");
        }

        return comparatorService.findSubstitutes(name, parsedDate);
    }

}
