package com.example.ble.DB.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "tb_device")
public class Device {
    @PrimaryKey(autoGenerate = true)
    public int device_idx;
    public String device_mac;
    public Date created_at;
    public Date exported_at;
}
