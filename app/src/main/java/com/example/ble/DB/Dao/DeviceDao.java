package com.example.ble.DB.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ble.DB.Entity.Device;

import java.util.List;

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM tb_device")
    List<Device> getAll();

    @Insert
    void insert(Device device);
}
