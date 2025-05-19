package com.accesa.pricecomparator.service;

import com.accesa.pricecomparator.repository.ProductRepositoryInMemory;
import com.accesa.pricecomparator.repository.DiscountRepositoryInMemory;
import com.accesa.pricecomparator.util.CsvProductLoader;
import com.accesa.pricecomparator.util.CsvDiscountLoader;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataLoaderService {

    private final CsvProductLoader productLoader;
    private final CsvDiscountLoader discountLoader;
    private final ProductRepositoryInMemory productRepo;
    private final DiscountRepositoryInMemory discountRepo;

    public DataLoaderService(CsvProductLoader productLoader,
                             CsvDiscountLoader discountLoader,
                             ProductRepositoryInMemory productRepo,
                             DiscountRepositoryInMemory discountRepo) {
        this.productLoader = productLoader;
        this.discountLoader = discountLoader;
        this.productRepo = productRepo;
        this.discountRepo = discountRepo;
    }

    @PostConstruct
    public void loadAllData() throws IOException, URISyntaxException {
        Path csvDir = Paths.get(getClass().getResource("/data/csv").toURI());

        Pattern productPattern = Pattern.compile("(lidl|profi|kaufland)_(\\d{4}-\\d{2}-\\d{2})\\.csv");
        Pattern discountPattern = Pattern.compile("(lidl|profi|kaufland)_discounts?_(\\d{4}-\\d{2}-\\d{2})\\.csv");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(csvDir)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();

                Matcher productMatcher = productPattern.matcher(fileName);
                Matcher discountMatcher = discountPattern.matcher(fileName);

                if (productMatcher.matches()) {
                    String store = productMatcher.group(1);
                    LocalDate date = LocalDate.parse(productMatcher.group(2));
                    var products = productLoader.loadProductsFromCsv(fileName, store, date);
                    productRepo.addProducts(store, products);
                }

                if (discountMatcher.matches()) {
                    String store = discountMatcher.group(1);
                    var discounts = discountLoader.loadDiscountsFromCsv(fileName, store);
                    discountRepo.addDiscounts(store, discounts);
                }
            }
        }
    }
}
