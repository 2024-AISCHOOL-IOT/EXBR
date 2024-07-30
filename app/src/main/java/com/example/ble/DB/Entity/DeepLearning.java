package com.example.ble.DB.Entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.sql.Timestamp;

@Entity(tableName = "tb_deep_learning",
        foreignKeys = @ForeignKey(entity = Device.class,
                parentColumns = "device_mac",
                childColumns = "device_mac",
                onDelete = ForeignKey.CASCADE))
public class DeepLearning {
    @PrimaryKey(autoGenerate = true)
    public int model_idx;
    public String sex;
    public String device_mac;
    public String model_name;
    public String analisys_result;
    public float prediction_rate;
    public Timestamp created_at;
}
