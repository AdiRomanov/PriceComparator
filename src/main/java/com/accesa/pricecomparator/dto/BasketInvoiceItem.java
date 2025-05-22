package com.accesa.pricecomparator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BasketInvoiceItem {
    private String productName;
    private String store;
    private double originalPrice;
    private double finalPrice;
    private double savings;
}
