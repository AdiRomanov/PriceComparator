package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.exception.ResourceNotFoundException;
import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
import com.accesa.pricecomparator.service.PriceComparatorService;
import com.accesa.pricecomparator.util.CsvProductLoader;
import org.springframework.web.bind.annotation.*;
import com.accesa.pricecomparator.dto.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Products", description = "Product search, filters and comparisons")
@RequestMapping("/api/products")
public class ProductController {

    private final CsvProductLoader csvLoader;
    private final ProductRepositoryInMemory productRepo;
    private final PriceComparatorService comparatorService;
    private final DiscountRepositoryInMemory discountRepo;



    public ProductController(CsvProductLoader csvLoader, ProductRepositoryInMemory productRepo,
                             PriceComparatorService comparatorService, DiscountRepositoryInMemory discountRepo) {
        this.csvLoader = csvLoader;
        this.productRepo = productRepo;
        this.comparatorService = comparatorService;
        this.discountRepo = discountRepo;
    }

    @Operation(summary = "Load sample products from a CSV file")
    @GetMapping("/from-csv")
    public List<Product> getProductsFromCsv() {
        return csvLoader.loadProductsFromCsv("lidl_2025-05-08.csv", "Lidl", LocalDate.of(2025, 5, 8));
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
        return productRepo.getAll();
    }

    @Operation(summary = "Get all products from a specific store")
    @GetMapping("/store/{store}")
    public List<Product> getProductsByStore(@PathVariable String store) {
        return productRepo.getByStore(store.toLowerCase());
    }

    @Operation(summary = "Find the cheapest product by name for a specific date")
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


    @Operation(summary = "Get full price history for a product across all dates")
    @GetMapping("/price-history")
    public List<PriceHistoryEntry> getPriceHistory(@RequestParam String name) {
        List<PriceHistoryEntry> history = comparatorService.getPriceHistoryForProduct(name);
        if (history.isEmpty()) {
            throw new ResourceNotFoundException("No price history found for product: " + name);
        }
        return history;
    }

    @Operation(summary = "Suggest substitute products with similar packaging and category")
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

    @Operation(summary = "Search products by name fragment")
    @ApiResponse(responseCode = "200", description = "List of matching products")
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String query) {
        List<Product> results = productRepo.searchByName(query);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("No products found for query: " + query);
        }
        return results;
    }


    @Operation(summary = "List all unique product brands available in the system")
    @GetMapping("/brands")
    public Set<String> getAllBrands() {
        return productRepo.getAllBrands();
    }


    @Operation(summary = "Get all products by a given brand on a specific date (includes discount info)")
    @GetMapping("/by-brand")
    public List<ProductWithDiscountView> getProductsByBrand(@RequestParam String brand,
                                                            @RequestParam String date) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Invalid date format.");
        }

        return comparatorService.getProductsByBrand(brand, parsedDate);
    }


    @Operation(summary = "Get all products from a specific category on a given day")
    @GetMapping("/by-category")
    public List<Product> getByCategory(@RequestParam String category,
                                       @RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return productRepo.getAll().stream()
                .filter(p -> p.getProductCategory().equalsIgnoreCase(category))
                .filter(p -> p.getDate().isEqual(parsedDate))
                .toList();
    }


    @Operation(summary = "Get all products below a given price on a given day")
    @GetMapping("/under-price")
    public List<Product> getUnderPrice(@RequestParam double max,
                                       @RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return productRepo.getAll().stream()
                .filter(p -> p.getDate().isEqual(parsedDate))
                .filter(p -> p.getPrice() <= max)
                .toList();
    }


    @Operation(summary = "List all stores where a given product is available")
    @GetMapping("/multi-store")
    public Set<String> getStoresWithProduct(@RequestParam String name) {
        return productRepo.getAll().stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(name))
                .map(Product::getStore)
                .collect(Collectors.toSet());
    }


    @Operation(summary = "List products sorted by price per unit (e.g. RON/l or RON/kg)")
    @GetMapping("/sorted-by-unit-price")
    public List<Product> getSortedByUnitPrice(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return productRepo.getAll().stream()
                .filter(p -> p.getDate().isEqual(parsedDate))
                .sorted(Comparator.comparingDouble(p -> p.getPrice() / p.getPackageQuantity()))
                .toList();
    }


    @GetMapping("/no-discount")
    public List<Product> getProductsWithoutDiscount(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        List<Discount> activeDiscounts = discountRepo.getAll().stream()
                .filter(d -> !parsedDate.isBefore(d.getFromDate()) && !parsedDate.isAfter(d.getToDate()))
                .toList();

        return productRepo.getAll().stream()
                .filter(p -> p.getDate().isEqual(parsedDate))
                .filter(p -> activeDiscounts.stream().noneMatch(d ->
                        d.getProductName().equalsIgnoreCase(p.getProductName()) &&
                                d.getStore().equalsIgnoreCase(p.getStore())))
                .toList();
    }


    @Operation(summary = "Compare two products by price and unit price on a specific date")
    @GetMapping("/compare")
    public ProductComparisonResult compareProducts(@RequestParam String name1,
                                                   @RequestParam String name2,
                                                   @RequestParam String date) {
        LocalDate d = LocalDate.parse(date);

        Optional<Product> p1 = productRepo.getAll().stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(name1) && p.getDate().isEqual(d))
                .findFirst();
        Optional<Product> p2 = productRepo.getAll().stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(name2) && p.getDate().isEqual(d))
                .findFirst();

        if (p1.isEmpty() || p2.isEmpty()) throw new ResourceNotFoundException("One of the products not found.");

        Product prod1 = p1.get();
        Product prod2 = p2.get();

        double ppu1 = prod1.getPrice() / prod1.getPackageQuantity();
        double ppu2 = prod2.getPrice() / prod2.getPackageQuantity();

        String cheaper = ppu1 < ppu2 ? name1 : (ppu1 > ppu2 ? name2 : "equal");

        return new ProductComparisonResult(
                name1, prod1.getPrice(), ppu1,
                name2, prod2.getPrice(), ppu2,
                cheaper
        );
    }


}
