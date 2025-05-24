package com.accesa.pricecomparator.service;

import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.model.PriceAlert;
import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.repository.PriceAlertRepositoryInMemory;
import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PriceAlertService {

    private final PriceAlertRepositoryInMemory alertRepo;
    private final ProductRepositoryInMemory productRepo;
    private final DiscountRepositoryInMemory discountRepo;

    public PriceAlertService(PriceAlertRepositoryInMemory alertRepo,
                             ProductRepositoryInMemory productRepo,
                             DiscountRepositoryInMemory discountRepo) {
        this.alertRepo = alertRepo;
        this.productRepo = productRepo;
        this.discountRepo = discountRepo;
    }

    public void saveAlert(PriceAlert alert) {
        alertRepo.addAlert(alert);
    }

    public List<PriceAlert> getTriggeredAlerts(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);

        List<Product> products = productRepo.getAll().stream()
                .filter(p -> p.getDate().isEqual(date))
                .toList();

        List<Discount> discounts = discountRepo.getAll().stream()
                .filter(d -> !date.isBefore(d.getFromDate()) && !date.isAfter(d.getToDate()))
                .toList();

        return alertRepo.getAll().stream()
                .filter(alert -> products.stream().anyMatch(p -> {
                    if (!p.getProductName().equalsIgnoreCase(alert.getProductName())) return false;

                    double finalPrice = p.getPrice();
                    for (Discount d : discounts) {
                        if (d.getProductName().equalsIgnoreCase(p.getProductName()) &&
                                d.getStore().equalsIgnoreCase(p.getStore())) {
                            finalPrice -= finalPrice * d.getPercentageOfDiscount() / 100.0;
                            break;
                        }
                    }

                    return finalPrice <= alert.getTargetPrice();
                }))
                .toList();
    }
}
