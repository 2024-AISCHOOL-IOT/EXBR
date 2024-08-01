package com.example.ble.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_model")
public class ModelData {
    @PrimaryKey(autoGenerate = true)
    private int model_idx;
    private String sex;
    private String device_mac;
    private String model_name;
    private byte[] model_data;
    private String analysis_result;
    private float prediction_rate;
    private String created_at;

    public ModelData(String sex, String device_mac, String model_name, byte[] model_data,
                     String analysis_result, float prediction_rate, String created_at) {
        this.sex = sex;
        this.device_mac = device_mac;
        this.model_name = model_name;
        this.model_data = model_data;
        this.analysis_result = analysis_result;
        this.prediction_rate = prediction_rate;
        this.created_at = created_at;
    }

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

    public byte[] getModel_data() {
        return model_data;
    }

    public void setModel_data(byte[] model_data) {
        this.model_data = model_data;
    }

    public String getAnalysis_result() {
        return analysis_result;
    }

    public void setAnalysis_result(String analysis_result) {
        this.analysis_result = analysis_result;
    }

    public float getPrediction_rate() {
        return prediction_rate;
    }

    public void setPrediction_rate(float prediction_rate) {
        this.prediction_rate = prediction_rate;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
