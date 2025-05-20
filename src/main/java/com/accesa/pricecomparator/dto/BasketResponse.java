package com.accesa.pricecomparator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BasketResponse {
    private List<BasketItemResponse> items;
    private double totalPrice;
    private List<SuggestedSubstitution> suggestions;

}
