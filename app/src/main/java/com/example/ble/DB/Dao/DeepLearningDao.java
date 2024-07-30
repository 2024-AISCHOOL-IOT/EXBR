package com.example.ble.DB.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.ble.DB.Entity.DeepLearning;

import java.util.List;

@Dao
public interface DeepLearningDao {
    @Query("SELECT * FROM tb_deep_learning")
    List<DeepLearning> getAll();

    @Insert
    void insert(DeepLearning deepLearning);
}
