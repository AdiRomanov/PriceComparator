package com.accesa.pricecomparator.util;

import com.accesa.pricecomparator.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateUtils {

    public static LocalDate parse(String input) {
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException e) {
            throw new ResourceNotFoundException("Invalid date format. Expected yyyy-MM-dd.");
        }
    }
}
