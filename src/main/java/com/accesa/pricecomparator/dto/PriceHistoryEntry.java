package com.accesa.pricecomparator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PriceHistoryEntry {
    private LocalDate date;
    private String store;
    private double originalPrice;
    private double finalPrice;
}
