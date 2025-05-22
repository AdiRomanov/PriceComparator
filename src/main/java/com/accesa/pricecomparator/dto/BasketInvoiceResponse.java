package com.accesa.pricecomparator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BasketInvoiceResponse {
    private List<BasketInvoiceItem> items;
    private double total;
    private double totalSavings;
}