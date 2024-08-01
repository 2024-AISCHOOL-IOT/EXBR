package com.example.ble.data;

import androidx.room.TypeConverter;

import java.sql.Timestamp;

public class TimestampConverter {

    @TypeConverter
    public static Timestamp fromTimestamp(Long value) {
        if (value == null) {
            return null;
        }
        return new Timestamp(value);
    }

    @TypeConverter
    public static Long timestampToLong(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.getTime();
    }
}
