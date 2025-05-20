package com.accesa.pricecomparator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuggestedSubstitution {
    private String originalProductName;
    private String originalBrand;
    private String suggestedProductName;
    private String suggestedBrand;
    private String store;
    private double originalFinalPrice;
    private double suggestedFinalPrice;
    private double savings;
}
