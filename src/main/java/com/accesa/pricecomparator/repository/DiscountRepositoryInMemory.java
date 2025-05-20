package com.accesa.pricecomparator.repository;

import com.accesa.pricecomparator.model.Discount;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public class DiscountRepositoryInMemory {
    private final Map<String, List<Discount>> storeDiscounts = new HashMap<>();

    public void addDiscounts(String store, List<Discount> discounts) {
        storeDiscounts.computeIfAbsent(store, k -> new ArrayList<>()).addAll(discounts);
    }

    public List<Discount> getAll() {
        return storeDiscounts.values().stream().flatMap(List::stream).toList();
    }

    public List<Discount> getByStore(String store) {
        return storeDiscounts.getOrDefault(store, List.of());
    }

    public List<Discount> getActiveDiscounts() {
        LocalDate today = LocalDate.of(2025, 5, 8); // LocalDate.now(); Hardcoded for testing!
        return getAll().stream()
                .filter(d -> (d.getFromDate().isEqual(today) || d.getFromDate().isBefore(today)) &&
                        (d.getToDate().isEqual(today) || d.getToDate().isAfter(today)))
                .toList();
    }

    public List<Discount> getNewDiscounts(LocalDate date) {
        return getAll().stream()
                .filter(d -> d.getFromDate().isEqual(date))
                .toList();
    }

}
