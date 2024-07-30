package com.example.ble.DB.Entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "tb_device", indices = {@Index(value = "device_mac", unique = true)})
public class Device {
    @PrimaryKey(autoGenerate = true)
    public int device_idx;
    public String device_mac;
    public Date created_at;
    public Date exported_at;

    // Getters and setters

    public int getDevice_idx() {
        return device_idx;
    }

    public void setDevice_idx(int device_idx) {
        this.device_idx = device_idx;
    }

    public String getDevice_mac() {
        return device_mac;
    }

    public void setDevice_mac(String device_mac) {
        this.device_mac = device_mac;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getExported_at() {
        return exported_at;
    }

    public void setExported_at(Date exported_at) {
        this.exported_at = exported_at;
    }
}
