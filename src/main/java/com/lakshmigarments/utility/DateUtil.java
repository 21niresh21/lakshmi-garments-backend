package com.lakshmigarments.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter INPUT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final DateTimeFormatter OUTPUT =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    public static String formatToShortDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isBlank()) {
            return "-";
        }
        LocalDateTime dateTime = LocalDateTime.parse(isoDateTime, INPUT);
        return dateTime.format(OUTPUT);
    }
}
