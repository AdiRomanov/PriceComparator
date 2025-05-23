package com.accesa.pricecomparator.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BudgetRequest {
    private double maxBudget;
    private List<String> categories;
    private LocalDate date;
}
