package com.accesa.pricecomparator.service;

import com.accesa.pricecomparator.dto.ProductComparisonResult;
import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
import com.accesa.pricecomparator.util.CsvProductLoader;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepositoryInMemory productRepo;
    private final DiscountRepositoryInMemory discountRepo;
    private final CsvProductLoader csvLoader;

    public ProductService(ProductRepositoryInMemory productRepo,
                          DiscountRepositoryInMemory discountRepo,
                          CsvProductLoader csvLoader) {
        this.productRepo = productRepo;
        this.discountRepo = discountRepo;
        this.csvLoader = csvLoader;
    }

    public List<Product> loadProductsFromCsv() {
        return csvLoader.loadProductsFromCsv("lidl_2025-05-08.csv", "Lidl", LocalDate.of(2025, 5, 8));
    }

    public List<Product> getAll() {
        return productRepo.getAll();
    }

    public List<Product> getByStore(String store) {
        return productRepo.getByStore(store.toLowerCase());
    }

    public Set<String> getAllBrands() {
        return productRepo.getAllBrands();
    }

    public List<Product> searchByName(String query) {
        return productRepo.searchByName(query);
    }

    public List<Product> getByCategory(String category, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return productRepo.getAll().stream()
                .filter(p -> p.getProductCategory().equalsIgnoreCase(category))
                .filter(p -> p.getDate().isEqual(date))
                .toList();
    }

    public List<Product> getUnderPrice(double maxPrice, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return productRepo.getAll().stream()
                .filter(p -> p.getDate().isEqual(date))
                .filter(p -> p.getPrice() <= maxPrice)
                .toList();
    }

    public List<Product> getSortedByUnitPrice(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        return productRepo.getAll().stream()
                .filter(p -> p.getDate().isEqual(date))
                .sorted(Comparator.comparingDouble(p -> p.getPrice() / p.getPackageQuantity()))
                .toList();
    }

    public List<Product> getProductsWithoutDiscount(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        List<Discount> discounts = discountRepo.getAll().stream()
                .filter(d -> !date.isBefore(d.getFromDate()) && !date.isAfter(d.getToDate()))
                .toList();

        return productRepo.getAll().stream()
                .filter(p -> p.getDate().isEqual(date))
                .filter(p -> discounts.stream().noneMatch(d ->
                        d.getProductName().equalsIgnoreCase(p.getProductName())
                                && d.getStore().equalsIgnoreCase(p.getStore())))
                .toList();
    }

    public Set<String> getStoresWithProduct(String productName) {
        return productRepo.getAll().stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(productName))
                .map(Product::getStore)
                .collect(Collectors.toSet());
    }

    public ProductComparisonResult compareProducts(String name1, String name2, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);

        Optional<Product> p1 = productRepo.getAll().stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(name1) && p.getDate().isEqual(date))
                .findFirst();
        Optional<Product> p2 = productRepo.getAll().stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(name2) && p.getDate().isEqual(date))
                .findFirst();

        if (p1.isEmpty() || p2.isEmpty()) return null;

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
