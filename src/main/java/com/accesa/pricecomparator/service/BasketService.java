package com.accesa.pricecomparator.service;

import com.accesa.pricecomparator.dto.*;
import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
import com.accesa.pricecomparator.util.DateUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;


@Service
public class BasketService {

    private final ProductRepositoryInMemory productRepo;
    private final DiscountRepositoryInMemory discountRepo;

    public BasketService(ProductRepositoryInMemory productRepo,
                         DiscountRepositoryInMemory discountRepo) {
        this.productRepo = productRepo;
        this.discountRepo = discountRepo;
    }

    public BasketInvoiceResponse getInvoice(BasketRequest request) {
        LocalDate date = DateUtils.parse(request.getDate());

        List<Product> all = productRepo.getAll();
        List<Discount> discounts = discountRepo.getAll();

        List<BasketInvoiceItem> items = new ArrayList<>();
        double total = 0;
        double saved = 0;

        for (String name : request.getProductNames()) {
            Optional<Product> cheapest = all.stream()
                    .filter(p -> p.getProductName().equalsIgnoreCase(name) && p.getDate().isEqual(date))
                    .map(p -> applyDiscount(p, discounts, date))
                    .min(Comparator.comparingDouble(Product::getPrice));

            if (cheapest.isPresent()) {
                Product p = cheapest.get();
                double orig = all.stream()
                        .filter(prod -> prod.getProductName().equalsIgnoreCase(name)
                                && prod.getStore().equalsIgnoreCase(p.getStore())
                                && prod.getDate().isEqual(date))
                        .mapToDouble(Product::getPrice)
                        .findFirst()
                        .orElse(p.getPrice());

                double diff = orig - p.getPrice();
                total += p.getPrice();
                saved += diff;

                items.add(new BasketInvoiceItem(p.getProductName(), p.getStore(), orig, p.getPrice(), diff));
            }
        }

        return new BasketInvoiceResponse(items, total, saved);
    }

    public BasketResponse getWithinBudget(BudgetRequest request) {
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

    public Map<LocalDate, Double> compareBasketAcrossDays(BasketComparisonRequest req) {
        List<Product> all = productRepo.getAll();
        List<Discount> discounts = discountRepo.getAll();

        Map<LocalDate, Double> result = new TreeMap<>();

        for (LocalDate date : req.getDates()) {
            double total = 0;

            for (String name : req.getProductNames()) {
                Optional<Product> cheapest = all.stream()
                        .filter(p -> p.getProductName().equalsIgnoreCase(name) && p.getDate().isEqual(date))
                        .map(p -> applyDiscount(p, discounts, date))
                        .min(Comparator.comparingDouble(Product::getPrice));

                total += cheapest.map(Product::getPrice).orElse(0.0);
            }

            result.put(date, total);
        }

        return result;
    }

    private Product applyDiscount(Product p, List<Discount> discounts, LocalDate date) {
        double price = p.getPrice();

        Discount d = discounts.stream()
                .filter(dis -> dis.getProductName().equalsIgnoreCase(p.getProductName())
                        && dis.getStore().equalsIgnoreCase(p.getStore())
                        && !date.isBefore(dis.getFromDate()) && !date.isAfter(dis.getToDate()))
                .findFirst()
                .orElse(null);

        if (d != null) {
            price -= price * d.getPercentageOfDiscount() / 100.0;
        }

        return new Product(
                p.getProductId(), p.getProductName(), p.getProductCategory(), p.getBrand(),
                p.getPackageQuantity(), p.getPackageUnit(), price,
                p.getCurrency(), p.getDate(), p.getStore()
        );
    }
}
