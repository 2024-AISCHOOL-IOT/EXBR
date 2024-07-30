package com.example.ble.DB.Entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.sql.Timestamp;

@Entity(tableName = "tb_sensing",
        foreignKeys = @ForeignKey(entity = Device.class,
                parentColumns = "device_mac",
                childColumns = "device_mac",
                onDelete = ForeignKey.CASCADE))
public class Sensing {
    @PrimaryKey(autoGenerate = true)
    public int sensing_idx;
    public String device_mac;
    public int middle_flex_sensor;
    public int middle_pressure_sensor;
    public int ring_flex_sensor;
    public int ring_pressure_sensor;
    public int pinky_flex_sensor;
    public int acceleration;
    public int gyroscope;
    public int magnetic_field;
    private long timestamp;

    public int getSensing_idx() {
        return sensing_idx;
    }

    public void setSensing_idx(int sensing_idx) {
        this.sensing_idx = sensing_idx;
    }

    public String getDevice_mac() {
        return device_mac;
    }

    public void setDevice_mac(String device_mac) {
        this.device_mac = device_mac;
    }

    public int getMiddle_flex_sensor() {
        return middle_flex_sensor;
    }

    public void setMiddle_flex_sensor(int middle_flex_sensor) {
        this.middle_flex_sensor = middle_flex_sensor;
    }

    public int getMiddle_pressure_sensor() {
        return middle_pressure_sensor;
    }

    public void setMiddle_pressure_sensor(int middle_pressure_sensor) {
        this.middle_pressure_sensor = middle_pressure_sensor;
    }

    public int getRing_flex_sensor() {
        return ring_flex_sensor;
    }

    public void setRing_flex_sensor(int ring_flex_sensor) {
        this.ring_flex_sensor = ring_flex_sensor;
    }

    public int getRing_pressure_sensor() {
        return ring_pressure_sensor;
    }

    public void setRing_pressure_sensor(int ring_pressure_sensor) {
        this.ring_pressure_sensor = ring_pressure_sensor;
    }

    public int getPinky_flex_sensor() {
        return pinky_flex_sensor;
    }

    public void setPinky_flex_sensor(int pinky_flex_sensor) {
        this.pinky_flex_sensor = pinky_flex_sensor;
    }

    public int getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(int acceleration) {
        this.acceleration = acceleration;
    }

    public int getGyroscope() {
        return gyroscope;
    }

    public void setGyroscope(int gyroscope) {
        this.gyroscope = gyroscope;
    }

    public int getMagnetic_field() {
        return magnetic_field;
    }

    public void setMagnetic_field(int magnetic_field) {
        this.magnetic_field = magnetic_field;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

