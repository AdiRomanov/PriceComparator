package com.accesa.pricecomparator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductComparisonResult {
    private String product1;
    private double price1;
    private double pricePerUnit1;

    private String product2;
    private double price2;
    private double pricePerUnit2;

    private String cheaper;
}
