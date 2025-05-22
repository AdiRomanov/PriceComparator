package com.accesa.pricecomparator.controller;

import com.accesa.pricecomparator.dto.BasketInvoiceItem;
import com.accesa.pricecomparator.dto.BasketInvoiceResponse;
import com.accesa.pricecomparator.dto.BasketRequest;
import com.accesa.pricecomparator.dto.BasketResponse;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/basket")
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


}