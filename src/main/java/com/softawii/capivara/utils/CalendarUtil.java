package com.softawii.capivara.utils;

import com.google.api.client.util.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CalendarUtil {

    public static final DateTimeFormatter RFC_3339_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
    public static final ZoneId            ZONE_ID            = ZoneId.of("GMT");

    public static DateTime getMinDateTime(int daysToIncrement) {
        LocalTime time = LocalTime.MIN;
        LocalDate date = LocalDate.now(ZONE_ID).plusDays(daysToIncrement);

        ZonedDateTime zonedDateTime = ZonedDateTime.of(date, time, ZONE_ID);
        String        rfc3339String = zonedDateTime.format(RFC_3339_FORMATTER);

        return DateTime.parseRfc3339(rfc3339String);
    }

    public static DateTime getMaxDateTime(int daysToIncrement) {
        LocalTime time = LocalTime.MAX;
        LocalDate date = LocalDate.now(ZONE_ID).plusDays(daysToIncrement);

        ZonedDateTime zonedDateTime = ZonedDateTime.of(date, time, ZONE_ID);
        String        rfc3339String = zonedDateTime.format(RFC_3339_FORMATTER);

        return DateTime.parseRfc3339(rfc3339String);
    }
}
