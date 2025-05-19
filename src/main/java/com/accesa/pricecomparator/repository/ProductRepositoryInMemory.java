package com.accesa.pricecomparator.repository;

import com.accesa.pricecomparator.model.Product;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ProductRepositoryInMemory {
    private final Map<String, List<Product>> storeProducts = new HashMap<>();

    public void addProducts(String store, List<Product> products) {
        storeProducts.computeIfAbsent(store, k -> new ArrayList<>()).addAll(products);
    }

    public List<Product> getAll() {
        return storeProducts.values().stream().flatMap(List::stream).toList();
    }

    public List<Product> getByStore(String store) {
        return storeProducts.getOrDefault(store, List.of());
    }
}
