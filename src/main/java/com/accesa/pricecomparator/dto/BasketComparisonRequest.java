package com.accesa.pricecomparator.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BasketComparisonRequest {
    private List<String> productNames;
    private List<LocalDate> dates;
}
