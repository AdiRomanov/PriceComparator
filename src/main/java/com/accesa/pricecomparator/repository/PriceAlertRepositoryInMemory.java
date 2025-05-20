package com.accesa.pricecomparator.repository;

import com.accesa.pricecomparator.model.PriceAlert;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class PriceAlertRepositoryInMemory {
    private final List<PriceAlert> alerts = new ArrayList<>();

    public void addAlert(PriceAlert alert) {
        alerts.add(alert);
    }

    public List<PriceAlert> getAll() {
        return alerts;
    }
}
