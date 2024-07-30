package com.example.ble.DB;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.example.ble.DB.Dao.AssistanceDao;
import com.example.ble.DB.Dao.DeepLearningDao;
import com.example.ble.DB.Dao.DeviceDao;
import com.example.ble.DB.Dao.SensingDao;
import com.example.ble.DB.Entity.Assistance;
import com.example.ble.DB.Entity.DeepLearning;
import com.example.ble.DB.Entity.Device;
import com.example.ble.DB.Entity.Sensing;

@Database(entities = {Assistance.class, Device.class, DeepLearning.class, Sensing.class}, version = 1)
@TypeConverters({Converter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract AssistanceDao assistanceDao();
    public abstract DeviceDao deviceDao();
    public abstract DeepLearningDao deepLearningDao();
    public abstract SensingDao sensingDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "EXBR_DB.db")
                            .createFromAsset("EXBR_DB.db") // 에셋 폴더의 데이터베이스 파일 사용
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
