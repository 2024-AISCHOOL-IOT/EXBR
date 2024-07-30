package com.example.ble.DB.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ble.DB.Entity.Sensing;

import java.util.List;

@Dao
public interface SensingDao {
    @Query("SELECT * FROM tb_sensing")
    List<Sensing> getAll();

    @Insert
    void insert(Sensing sensing);
}
