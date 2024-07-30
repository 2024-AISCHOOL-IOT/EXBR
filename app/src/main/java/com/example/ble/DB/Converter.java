package com.example.ble.DB;

import androidx.room.TypeConverter;

import java.sql.Timestamp;
import java.util.Date;

public class Converter {
    @TypeConverter
    public static Timestamp fromTimestamp(Long value) {
        return value == null ? null : new Timestamp(value);
    }

    @TypeConverter
    public static Long timestampToLong(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.getTime();
    }

    @TypeConverter
    public static Date fromDateLong(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToLong(Date date) {
        return date == null ? null : date.getTime();
    }
}
