package com.example.ble.DB.Entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.sql.Timestamp;

@Entity(tableName = "tb_assistance")
public class Assistance {
    @PrimaryKey(autoGenerate = true)
    public int assist_idx;
    public String sex;
    public int user_idx;
    public String gesture_type;
    public int assisted_value;
    public Timestamp created_at;
}

