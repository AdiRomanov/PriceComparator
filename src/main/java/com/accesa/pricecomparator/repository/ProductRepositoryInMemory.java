package com.accesa.pricecomparator.repository;

import com.accesa.pricecomparator.model.Product;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

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

    public List<Product> searchByName(String query) {
        return getAll().stream()
                .filter(p -> p.getProductName().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    public Set<String> getAllBrands() {
        return getAll().stream()
                .map(Product::getBrand)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new)); // sortat alfabetic
    }


}
