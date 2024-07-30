package com.example.ble.DB.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ble.DB.Entity.Assistance;

import java.util.List;

@Dao
public interface AssistanceDao {
    @Query("SELECT * FROM tb_assistance")
    List<Assistance> getAll();

    @Insert
    void insert(Assistance assistance);
}
