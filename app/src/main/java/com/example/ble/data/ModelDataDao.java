package com.example.ble.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ModelDataDao {
    @Insert
    void insert(ModelData modelData);

    @Query("SELECT * FROM tb_model")
    List<ModelData> getAllModelData();
}
