package com.cloud_guest.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;

/**
 * @author yan
 * @date 2024/6/14 2:40
 */
public class DateUtils extends DateUtil {
    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    public static LocalDateTime longToLocalDateTime(long date) {
        return toLocalDateTime(date(date));
    }

    public static long LocalDateTimeTolong(LocalDateTime date) {
        return LocalDateTimeTolong(date, ZoneId.systemDefault());
    }

    public static long LocalDateTimeTolong(LocalDateTime date, ZoneId zoneId) {
        if (ObjectUtil.isEmpty(zoneId)) {
            zoneId = ZoneId.systemDefault();
        }
        return date.atZone(zoneId).toInstant().toEpochMilli();
    }

    public static Date LocalDateTimeToDate(LocalDateTime date) {
        return LocalDateTimeToDate(date, ZoneId.systemDefault());
    }

    public static Date LocalDateTimeToDate(LocalDateTime date, ZoneId zoneId) {
        if (ObjectUtil.isEmpty(zoneId)) {
            zoneId = ZoneId.systemDefault();
        }
        ZonedDateTime zdt = date.atZone(zoneId);
        return Date.from(zdt.toInstant());
    }

    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return org.apache.commons.lang3.time.DateUtils.parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    public static final String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 增加 LocalDateTime ==> Date
     */
    public static Date toDate(LocalDateTime temporalAccessor) {
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     * 增加 LocalDate ==> Date
     */
    public static Date toDate(LocalDate temporalAccessor) {
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }
}
