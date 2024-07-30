package com.example.ble.DB.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ble.DB.Entity.Sensing;

import java.util.List;

@Dao
public interface SensingDao {
    @Insert
    void insert(Sensing sensing);

    @Query("SELECT * FROM tb_sensing")
    List<Sensing> getAll();

    @Query("DELETE FROM tb_sensing")
    void deleteAll();
}
