package com.accesa.pricecomparator.service;

import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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



}
