package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.exception.ResourceNotFoundException;
import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.service.PriceComparatorService;
import com.accesa.pricecomparator.service.ProductService;
import com.accesa.pricecomparator.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.accesa.pricecomparator.dto.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.time.LocalDate;
import java.util.List;
import java.util.Set;


@Slf4j
@RestController
@Tag(name = "Products", description = "Product search, filters and comparisons")
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final PriceComparatorService comparatorService;


    public ProductController(ProductService productService,PriceComparatorService comparatorService) {
        this.comparatorService = comparatorService;
        this.productService = productService;
    }

    @Operation(summary = "Load sample products from a CSV file")
    @GetMapping("/from-csv")
    public List<Product> getProductsFromCsv() {
        return productService.loadProductsFromCsv();
    }

    @Operation(summary = "Return a sample hardcoded product (for testing)")
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

    @Operation(summary = "Get all products loaded into memory")
    @GetMapping("/all")
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productService.getAll();
    }

    @Operation(summary = "Get all products from a specific store")
    @GetMapping("/store/{store}")
    public List<Product> getProductsByStore(@PathVariable String store) {
        log.info("Fetching products from store: {}", store);
        return productService.getByStore(store);
    }


    @Operation(summary = "Find the cheapest product by name for a specific date")
    @GetMapping("/cheapest-by-name")
    public Product getCheapestProductByNameAndDate(@RequestParam String name,
                                                   @RequestParam String date) {
        log.info("Searching for cheapest product: '{}' on {}", name, date);
        LocalDate parsedDate = DateUtils.parse(date);
        return comparatorService.findCheapestStoreForProductByName(name, parsedDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product '" + name + "' not found for date " + date));
    }


    @Operation(summary = "Get full price history for a product across all dates")
    @GetMapping("/price-history")
    public List<PriceHistoryEntry> getPriceHistory(@RequestParam String name) {
        log.info("Retrieving price history for '{}'", name);
        List<PriceHistoryEntry> history = comparatorService.getPriceHistoryForProduct(name);
        if (history.isEmpty()) {
            throw new ResourceNotFoundException("No price history found for product: " + name);
        }
        return history;
    }

    @Operation(summary = "Suggest substitute products with similar packaging and category")
    @GetMapping("/substitutes")
    public List<Product> getSubstitutes(@RequestParam String name, @RequestParam String date) {
        log.info("Searching substitute for: '{}' on {}", name, date);
        LocalDate parsedDate = DateUtils.parse(date);
        return comparatorService.findSubstitutes(name, parsedDate);
    }

    @Operation(summary = "Search products by name fragment")
    @ApiResponse(responseCode = "200", description = "List of matching products")
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String query) {
        log.info("Searching products with query: '{}'", query);
        List<Product> results = productService.searchByName(query);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("No products found for query: " + query);
        }
        return results;
    }

    @Operation(summary = "List all unique product brands available in the system")
    @GetMapping("/brands")
    public Set<String> getAllBrands() {
        log.info("Fetching all unique brands");
        return productService.getAllBrands();
    }

    @Operation(summary = "Get all products by a given brand on a specific date (includes discount info)")
    @GetMapping("/by-brand")
    public List<ProductWithDiscountView> getProductsByBrand(@RequestParam String brand,
                                                            @RequestParam String date) {
        log.info("Fetching products by brand: '{}' on {}", brand, date);
        LocalDate parsedDate = DateUtils.parse(date);
        return comparatorService.getProductsByBrand(brand, parsedDate);
    }


    @Operation(summary = "Get all products from a specific category on a given day")
    @GetMapping("/by-category")
    public List<Product> getByCategory(@RequestParam String category, @RequestParam String date) {
        log.info("Fetching products by category: '{}' on {}", category, date);
        return productService.getByCategory(category, date);
    }


    @Operation(summary = "Get all products below a given price on a given day")
    @GetMapping("/under-price")
    public List<Product> getUnderPrice(@RequestParam double max, @RequestParam String date) {
        log.info("Fetching products below a given price on {}", date);
        return productService.getUnderPrice(max, date);
    }


    @Operation(summary = "List all stores where a given product is available")
    @GetMapping("/multi-store")
    public Set<String> getStoresWithProduct(@RequestParam String name) {
        log.info("Fetching stores with product: '{}'", name);

        return productService.getStoresWithProduct(name);
    }


    @Operation(summary = "List products sorted by price per unit (e.g. RON/l or RON/kg)")
    @GetMapping("/sorted-by-unit-price")
    public List<Product> getSortedByUnitPrice(@RequestParam String date) {
        log.info("Fetching products sorted by price per unit on {}", date);
        return productService.getSortedByUnitPrice(date);
    }

    @Operation(summary = "List products that have no discount available")
    @GetMapping("/no-discount")
    public List<Product> getProductsWithoutDiscount(@RequestParam String date) {
        log.info("Fetching products that have no discount available");
        return productService.getProductsWithoutDiscount(date);
    }


    @Operation(summary = "Compare two products by price and unit price on a specific date")
    @GetMapping("/compare")
    public ProductComparisonResult compareProducts(@RequestParam String name1,
                                                   @RequestParam String name2,
                                                   @RequestParam String date) {
        log.info("Comparing '{}' vs '{}' on {}", name1, name2, date);
        ProductComparisonResult result = productService.compareProducts(name1, name2, date);
        if (result == null)
            throw new ResourceNotFoundException("One of the products not found.");
        return result;
    }


}
