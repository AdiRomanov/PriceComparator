package com.accesa.pricecomparator.util;

import com.accesa.pricecomparator.model.Discount;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvDiscountLoader {

    public List<Discount> loadDiscountsFromCsv(String filename, String storeName) {
        List<Discount> discounts = new ArrayList<>();

        try (
                CSVReader reader = new CSVReaderBuilder(
                        new InputStreamReader(getClass().getResourceAsStream("/data/csv/" + filename)))
                        .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                        .build()
        ) {
            String[] line;
            reader.readNext(); // skip header

            while ((line = reader.readNext()) != null) {
                if (line.length < 9) continue;

                Discount discount = new Discount(
                        line[0], // productId
                        line[1], // productName
                        line[2], // brand
                        Double.parseDouble(line[3]), // packageQuantity
                        line[4], // packageUnit
                        line[5], // productCategory
                        LocalDate.parse(line[6]),    // fromDate
                        LocalDate.parse(line[7]),    // toDate
                        Integer.parseInt(line[8]),   // percentage
                        storeName                    // store
                );

                discounts.add(discount);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read discount CSV file: " + filename, e);
        }

        return discounts;
    }
}
