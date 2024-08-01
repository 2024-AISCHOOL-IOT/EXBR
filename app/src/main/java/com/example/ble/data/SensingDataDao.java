package com.example.ble.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SensingDataDao {
    @Insert
    void insert(SensingData sensingData);

    @Insert
    void insertAll(List<SensingData> sensingDataList);

    @Query("DELETE FROM tb_sensing")
    void clear();

    @Query("DELETE FROM sqlite_sequence WHERE name='tb_sensing'")
    void resetPrimaryKey();

    @Query("SELECT * FROM tb_sensing")
    List<SensingData> getAllSensingData();
}
