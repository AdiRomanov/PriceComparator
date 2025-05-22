package com.accesa.pricecomparator.service;

import com.accesa.pricecomparator.dto.*;
import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PriceComparatorService {

    private final ProductRepositoryInMemory productRepo;
    private final DiscountRepositoryInMemory discountRepo;

    public PriceComparatorService(ProductRepositoryInMemory productRepo, DiscountRepositoryInMemory discountRepo) {
        this.productRepo = productRepo;
        this.discountRepo = discountRepo;
    }


    public Optional<Product> findCheapestStoreForProductByName(String productName, LocalDate date) {

        List<Product> allProducts = productRepo.getAll();
        List<Discount> allDiscounts = discountRepo.getAll();

        return allProducts.stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(productName)
                        && p.getDate().isEqual(date))
                .map(p -> {
                    Discount matchingDiscount = allDiscounts.stream()
                            .filter(d -> d.getProductName().equalsIgnoreCase(p.getProductName())
                                    && d.getStore().equalsIgnoreCase(p.getStore())
                                    && (!date.isBefore(d.getFromDate()) && !date.isAfter(d.getToDate())))
                            .findFirst()
                            .orElse(null);

                    double discount = (matchingDiscount != null)
                            ? p.getPrice() * matchingDiscount.getPercentageOfDiscount() / 100.0
                            : 0.0;

                    double finalPrice = p.getPrice() - discount;

                    return new Product(
                            p.getProductId(),
                            p.getProductName(),
                            p.getProductCategory(),
                            p.getBrand(),
                            p.getPackageQuantity(),
                            p.getPackageUnit(),
                            finalPrice,
                            p.getCurrency(),
                            p.getDate(),
                            p.getStore()
                    );
                })
                .min(Comparator.comparingDouble(Product::getPrice));
    }

    public BasketResponse optimizeBasket(List<String> productNames, LocalDate date) {
        List<Discount> allDiscounts = discountRepo.getAll();
        List<Product> allProducts = productRepo.getAll();

        List<BasketItemResponse> items = new ArrayList<>();
        List<SuggestedSubstitution> suggestions = new ArrayList<>();
        double total = 0.0;

        for (String name : productNames) {
            // Caută produsul optim (cu reducere aplicată dacă există)
            Optional<Product> cheapest = allProducts.stream()
                    .filter(p -> p.getProductName().equalsIgnoreCase(name) && p.getDate().isEqual(date))
                    .map(p -> {
                        double finalPrice = p.getPrice();
                        Optional<Discount> discountOpt = allDiscounts.stream()
                                .filter(d -> d.getProductName().equalsIgnoreCase(p.getProductName())
                                        && d.getStore().equalsIgnoreCase(p.getStore())
                                        && !date.isBefore(d.getFromDate()) && !date.isAfter(d.getToDate()))
                                .findFirst();
                        if (discountOpt.isPresent()) {
                            finalPrice -= finalPrice * discountOpt.get().getPercentageOfDiscount() / 100.0;
                        }

                        return new Product(
                                p.getProductId(), p.getProductName(), p.getProductCategory(),
                                p.getBrand(), p.getPackageQuantity(), p.getPackageUnit(),
                                finalPrice, p.getCurrency(), p.getDate(), p.getStore()
                        );
                    })
                    .min(Comparator.comparingDouble(Product::getPrice));

            if (cheapest.isEmpty()) {
                items.add(new BasketItemResponse(name, "Not found", 0.0));
                continue;
            }

            Product selected = cheapest.get();
            items.add(new BasketItemResponse(selected.getProductName(), selected.getStore(), selected.getPrice()));
            total += selected.getPrice();

            // Sugestii de substituție (mai ieftine, dar similare)
            List<Product> substitutes = allProducts.stream()
                    .filter(p -> !p.getProductName().equalsIgnoreCase(name))
                    .filter(p -> p.getDate().isEqual(date))
                    .filter(p -> p.getProductCategory().equalsIgnoreCase(selected.getProductCategory()))
                    .filter(p -> p.getPackageUnit().equalsIgnoreCase(selected.getPackageUnit()))
                    .map(p -> {
                        double finalPrice = p.getPrice();
                        Optional<Discount> discountOpt = allDiscounts.stream()
                                .filter(d -> d.getProductName().equalsIgnoreCase(p.getProductName())
                                        && d.getStore().equalsIgnoreCase(p.getStore())
                                        && !date.isBefore(d.getFromDate()) && !date.isAfter(d.getToDate()))
                                .findFirst();
                        if (discountOpt.isPresent()) {
                            finalPrice -= finalPrice * discountOpt.get().getPercentageOfDiscount() / 100.0;
                        }

                        return new Product(
                                p.getProductId(), p.getProductName(), p.getProductCategory(),
                                p.getBrand(), p.getPackageQuantity(), p.getPackageUnit(),
                                finalPrice, p.getCurrency(), p.getDate(), p.getStore()
                        );
                    })
                    .filter(p -> {
                        double ppuOriginal = selected.getPrice() / selected.getPackageQuantity();
                        double ppuCandidate = p.getPrice() / p.getPackageQuantity();
                        return ppuCandidate < ppuOriginal * 0.95; // cu cel puțin 5% mai ieftin per unit
                    })
                    .sorted(Comparator.comparingDouble(p -> p.getPrice() / p.getPackageQuantity()))
                    .toList();

            if (!substitutes.isEmpty()) {
                Product sub = substitutes.get(0);
                double savings = selected.getPrice() - sub.getPrice();
                suggestions.add(new SuggestedSubstitution(
                        selected.getProductName(),
                        selected.getBrand(),
                        sub.getProductName(),
                        sub.getBrand(),
                        sub.getStore(),
                        selected.getPrice(),
                        sub.getPrice(),
                        savings
                ));
            }
        }

        return new BasketResponse(items, total, suggestions);
    }


    public List<PriceHistoryEntry> getPriceHistoryForProduct(String productName) {
        List<Product> allProducts = productRepo.getAll();
        List<Discount> allDiscounts = discountRepo.getAll();

        return allProducts.stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(productName))
                .map(p -> {
                    Discount discount = allDiscounts.stream()
                            .filter(d -> d.getProductName().equalsIgnoreCase(p.getProductName())
                                    && d.getStore().equalsIgnoreCase(p.getStore())
                                    && (!p.getDate().isBefore(d.getFromDate()) && !p.getDate().isAfter(d.getToDate())))
                            .findFirst()
                            .orElse(null);

                    double finalPrice = p.getPrice();
                    if (discount != null) {
                        finalPrice -= finalPrice * discount.getPercentageOfDiscount() / 100.0;
                    }

                    return new PriceHistoryEntry(
                            p.getDate(),
                            p.getStore(),
                            p.getPrice(),
                            finalPrice
                    );
                })
                .sorted(Comparator.comparing(PriceHistoryEntry::getDate))
                .toList();
    }


    public List<Product> findSubstitutes(String productName, LocalDate date) {
        List<Product> allProducts = productRepo.getAll();
        List<Discount> allDiscounts = discountRepo.getAll();

        // Găsim produsul original (oricare variantă din acea zi)
        Optional<Product> originalOpt = allProducts.stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(productName) && p.getDate().isEqual(date))
                .findFirst();

        if (originalOpt.isEmpty()) return List.of();
        Product original = originalOpt.get();

        double originalPricePerUnit = original.getPrice() / original.getPackageQuantity();

        return allProducts.stream()
                //.filter(p -> !p.getProductName().equalsIgnoreCase(productName)) // exclude original
                .filter(p -> p.getDate().isEqual(date))
                .filter(p -> p.getProductCategory().equalsIgnoreCase(original.getProductCategory()))
                .filter(p -> p.getPackageUnit().equalsIgnoreCase(original.getPackageUnit()))
                .map(p -> {
                    Discount discount = allDiscounts.stream()
                            .filter(d -> d.getProductName().equalsIgnoreCase(p.getProductName())
                                    && d.getStore().equalsIgnoreCase(p.getStore())
                                    && (!date.isBefore(d.getFromDate()) && !date.isAfter(d.getToDate())))
                            .findFirst()
                            .orElse(null);

                    double finalPrice = p.getPrice();
                    if (discount != null) {
                        finalPrice -= finalPrice * discount.getPercentageOfDiscount() / 100.0;
                    }

                    return new Product(
                            p.getProductId(),
                            p.getProductName(),
                            p.getProductCategory(),
                            p.getBrand(),
                            p.getPackageQuantity(),
                            p.getPackageUnit(),
                            finalPrice,
                            p.getCurrency(),
                            p.getDate(),
                            p.getStore()
                    );
                })
                .filter(p -> {
                    double pricePerUnit = p.getPrice() / p.getPackageQuantity();
                    return Math.abs(pricePerUnit - originalPricePerUnit) / originalPricePerUnit <= 0.10;
                })
                .toList();
    }

    public List<ProductWithDiscountView> getProductsByBrand(String brand, LocalDate date) {
        List<Product> products = productRepo.getAll().stream()
                .filter(p -> p.getBrand() != null && p.getBrand().equalsIgnoreCase(brand))
                .filter(p -> p.getDate().isEqual(date))
                .toList();

        List<Discount> discounts = discountRepo.getAll().stream()
                .filter(d -> !date.isBefore(d.getFromDate()) && !date.isAfter(d.getToDate()))
                .toList();

        return products.stream()
                .map(p -> {
                    double finalPrice = p.getPrice();
                    Optional<Discount> d = discounts.stream()
                            .filter(discount -> discount.getProductName().equalsIgnoreCase(p.getProductName())
                                    && discount.getStore().equalsIgnoreCase(p.getStore()))
                            .findFirst();

                    if (d.isPresent()) {
                        finalPrice -= finalPrice * d.get().getPercentageOfDiscount() / 100.0;
                    }

                    return new ProductWithDiscountView(
                            p.getProductName(),
                            p.getStore(),
                            p.getPrice(),
                            finalPrice
                    );
                })
                .toList();
    }


}
