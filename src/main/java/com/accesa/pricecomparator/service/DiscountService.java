package com.accesa.pricecomparator.service;

import com.accesa.pricecomparator.exception.ResourceNotFoundException;
import com.accesa.pricecomparator.model.Discount;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.util.CsvDiscountLoader;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class DiscountService {

    private final DiscountRepositoryInMemory discountRepo;
    private final CsvDiscountLoader discountLoader;

    public DiscountService(DiscountRepositoryInMemory discountRepo, CsvDiscountLoader discountLoader) {
        this.discountRepo = discountRepo;
        this.discountLoader = discountLoader;
    }

    public List<Discount> loadFromCsv() {
        return discountLoader.loadDiscountsFromCsv("lidl_discounts_2025-05-08.csv", "Lidl");
    }

    public List<Discount> getAll() {
        return discountRepo.getAll();
    }

    public List<Discount> getByStore(String store) {
        return discountRepo.getByStore(store.toLowerCase());
    }

    public List<Discount> getBestActive() {
        return discountRepo.getActiveDiscounts().stream()
                .sorted(Comparator.comparingInt(Discount::getPercentageOfDiscount).reversed())
                .limit(10)
                .toList();
    }

    public List<Discount> getNewDiscounts(String date) {
        LocalDate parsedDate = parseDate(date);
        return discountRepo.getNewDiscounts(parsedDate);
    }

    public List<Discount> getAbovePercentage(String date, double percent) {
        LocalDate parsedDate = parseDate(date);
        return discountRepo.getAll().stream()
                .filter(d -> !parsedDate.isBefore(d.getFromDate()) && !parsedDate.isAfter(d.getToDate()))
                .filter(d -> d.getPercentageOfDiscount() > percent)
                .toList();
    }

    public List<Discount> getExpiring(String date) {
        LocalDate parsedDate = parseDate(date);
        return discountRepo.getAll().stream()
                .filter(d -> d.getToDate().isEqual(parsedDate))
                .toList();
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Invalid date format. Use yyyy-MM-dd.");
        }
    }
}
