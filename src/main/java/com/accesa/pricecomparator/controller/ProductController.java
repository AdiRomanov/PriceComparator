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


import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
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


    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String query) {
        List<Product> results = productRepo.searchByName(query);
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("No products found for query: " + query);
        }
        return results;
    }

    @GetMapping("/brands")
    public Set<String> getAllBrands() {
        return productRepo.getAllBrands();
    }


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


    @GetMapping("/by-category")
    public List<Product> getByCategory(@RequestParam String category,
                                       @RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return productRepo.getAll().stream()
                .filter(p -> p.getProductCategory().equalsIgnoreCase(category))
                .filter(p -> p.getDate().isEqual(parsedDate))
                .toList();
    }


    @GetMapping("/under-price")
    public List<Product> getUnderPrice(@RequestParam double max,
                                       @RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return productRepo.getAll().stream()
                .filter(p -> p.getDate().isEqual(parsedDate))
                .filter(p -> p.getPrice() <= max)
                .toList();
    }


    @GetMapping("/multi-store")
    public Set<String> getStoresWithProduct(@RequestParam String name) {
        return productRepo.getAll().stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(name))
                .map(Product::getStore)
                .collect(Collectors.toSet());
    }

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
