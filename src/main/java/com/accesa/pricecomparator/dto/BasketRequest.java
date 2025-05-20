package com.accesa.pricecomparator.dto;

import java.util.List;

public class BasketRequest {
    private List<String> productNames;
    private String date;

    public List<String> getProductNames() {
        return productNames;
    }

    public void setProductNames(List<String> productNames) {
        this.productNames = productNames;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
