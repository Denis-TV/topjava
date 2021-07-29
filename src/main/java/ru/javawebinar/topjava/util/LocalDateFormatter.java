package ru.javawebinar.topjava.util;

import org.springframework.format.Formatter;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class LocalDateFormatter implements Formatter<LocalDate> {
    @Override
    public String print(LocalDate object, Locale locale) {
        if (object == null) {
            return "";
        }
        return object.format(DateTimeFormatter.ISO_DATE);
    }

    @Override
    public LocalDate parse(String text, Locale locale) throws ParseException {
        if (text == null) {
            return null;
        }
        return LocalDate.parse(text, DateTimeFormatter.ISO_DATE);
    }
}
