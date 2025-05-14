package com.accesa.pricecomparator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private String productId;
    private String productName;
    private String productCategory;
    private String brand;
    private double packageQuantity;
    private String packageUnit;
    private double price;
    private String currency;
    private LocalDate date;
    private String store;
}
