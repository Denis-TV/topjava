package ru.javawebinar.topjava.util;

import org.springframework.format.Formatter;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class LocalTimeFormatter implements Formatter<LocalTime> {
    @Override
    public String print(LocalTime object, Locale locale) {
        if (object == null) {
            return "";
        }
        return object.format(DateTimeFormatter.ISO_DATE);
    }

    @Override
    public LocalTime parse(String text, Locale locale) throws ParseException {
        if (text == null) {
            return null;
        }
        return LocalTime.parse(text, DateTimeFormatter.ISO_TIME);
    }
}
