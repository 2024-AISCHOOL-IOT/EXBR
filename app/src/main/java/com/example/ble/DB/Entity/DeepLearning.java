package com.example.ble.DB.Entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.sql.Timestamp;

@Entity(tableName = "tb_deep_learning",
        foreignKeys = @ForeignKey(entity = Device.class,
                parentColumns = "device_mac",
                childColumns = "device_mac",
                onDelete = ForeignKey.CASCADE))
public class DeepLearning {
    @PrimaryKey(autoGenerate = true)
    public int model_idx;
    public String sex;
    public String device_mac;
    public String model_name;
    public String analisys_result;

    public int getModel_idx() {
        return model_idx;
    }

    public void setModel_idx(int model_idx) {
        this.model_idx = model_idx;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDevice_mac() {
        return device_mac;
    }

    public void setDevice_mac(String device_mac) {
        this.device_mac = device_mac;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public String getAnalisys_result() {
        return analisys_result;
    }

    public void setAnalisys_result(String analisys_result) {
        this.analisys_result = analisys_result;
    }

    public float getPrediction_rate() {
        return prediction_rate;
    }

    public void setPrediction_rate(float prediction_rate) {
        this.prediction_rate = prediction_rate;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public float prediction_rate;
    public Timestamp created_at;
}
