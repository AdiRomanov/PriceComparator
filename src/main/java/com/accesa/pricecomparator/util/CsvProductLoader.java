package com.accesa.pricecomparator.util;

import com.accesa.pricecomparator.model.Product;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvProductLoader {

    public List<Product> loadProductsFromCsv(String filename, String storeName, LocalDate date) {
        List<Product> products = new ArrayList<>();

        try (
                CSVReader reader = new CSVReaderBuilder(
                        new InputStreamReader(getClass().getResourceAsStream("/data/csv/" + filename)))
                        .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                        .build()
        ) {
            String[] line;
            reader.readNext(); // skip header

            while ((line = reader.readNext()) != null) {
                if (line.length < 8) {
                    System.err.println("Invalid line (too short), skipping: " + String.join(";", line));
                    continue;
                }

                Product product = new Product(
                        line[0],
                        line[1],
                        line[2],
                        line[3],
                        Double.parseDouble(line[4]),
                        line[5],
                        Double.parseDouble(line[6]),
                        line[7],
                        date,
                        storeName
                );
                products.add(product);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file: " + filename, e);
        }

        return products;
    }
}
