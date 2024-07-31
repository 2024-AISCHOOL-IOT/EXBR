package com.example.ble.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_sensing")
public class SensingData {
    @PrimaryKey(autoGenerate = true)
    private int sensing_idx;
    private int middle_flex_sensor;
    private int middle_pressure_sensor;
    private int ring_flex_sensor;
    private int ring_pressure_sensor;
    private int pinky_flex_sensor;

    // 기본 생성자
    public SensingData() {}

    // 모든 필드를 포함한 생성자
    public SensingData(int middle_flex_sensor, int middle_pressure_sensor, int ring_flex_sensor,
                       int ring_pressure_sensor, int pinky_flex_sensor) {
        this.middle_flex_sensor = middle_flex_sensor;
        this.middle_pressure_sensor = middle_pressure_sensor;
        this.ring_flex_sensor = ring_flex_sensor;
        this.ring_pressure_sensor = ring_pressure_sensor;
        this.pinky_flex_sensor = pinky_flex_sensor;
    }

    public int getSensing_idx() {
        return sensing_idx;
    }

    public void setSensing_idx(int sensing_idx) {
        this.sensing_idx = sensing_idx;
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
}
