package org.nodocentral.miviaje.data.room.converters;

import androidx.room.TypeConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class DateConverters {
    @TypeConverter
    public static Long fromLocalDate(LocalDate date) {
        return date == null ? null : date.toEpochDay();
    }

    @TypeConverter
    public static LocalDate toLocalDate(Long value) {
        return value == null ? null : LocalDate.ofEpochDay(value);
    }

    @TypeConverter
    public static Integer fromLocalTime(LocalTime time) {
        return time == null ? null : time.toSecondOfDay();
    }

    @TypeConverter
    public static LocalTime toLocalTime(Integer seconds) {
        return seconds == null ? null : LocalTime.ofSecondOfDay(seconds);
    }

    @TypeConverter
    public static Long fromLocalDateTime(LocalDateTime date) {
        return date == null ? null : date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    @TypeConverter
    public static LocalDateTime toLocalDateTime(Long value) {
        return value == null ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }
}

