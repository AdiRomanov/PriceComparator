package com.accesa.pricecomparator.service;

import com.accesa.pricecomparator.model.Product;
import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final ProductRepositoryInMemory productRepo;

    public StatsService(ProductRepositoryInMemory productRepo) {
        this.productRepo = productRepo;
    }

    public Map<LocalDate, Double> getCategoryPriceTrend(String category, String store) {
        return productRepo.getAll().stream()
                .filter(p -> p.getProductCategory().equalsIgnoreCase(category))
                .filter(p -> p.getStore().equalsIgnoreCase(store))
                .collect(Collectors.groupingBy(
                        Product::getDate,
                        TreeMap::new,
                        Collectors.averagingDouble(Product::getPrice)
                ));
    }

    public Map<LocalDate, Double> getStoreDailyIndex(String store) {
        return productRepo.getByStore(store).stream()
                .collect(Collectors.groupingBy(
                        Product::getDate,
                        TreeMap::new,
                        Collectors.averagingDouble(Product::getPrice)
                ));
    }
}
