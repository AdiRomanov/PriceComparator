package com.accesa.pricecomparator.util;

import com.accesa.pricecomparator.model.Product;

import java.time.LocalDate;
import java.util.function.Predicate;

public class ProductPredicates {

    public static Predicate<Product> matchesNameAndDate(String name, LocalDate date) {
        return p -> p.getProductName().equalsIgnoreCase(name)
                && p.getDate().isEqual(date);
    }

}
