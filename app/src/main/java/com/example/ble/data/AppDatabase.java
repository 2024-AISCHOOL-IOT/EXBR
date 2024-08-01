package com.example.ble.data;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import android.content.Context;

// 데이터베이스 엔티티와 버전을 정의
@Database(entities = {SensingData.class, ModelData.class}, version = 1, exportSchema = false)
@TypeConverters({TimestampConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    // DAO 객체 정의
    public abstract SensingDataDao sensingDataDao();
    public abstract ModelDataDao modelDataDao();

    // AppDatabase 인스턴스를 저장할 변수
    private static volatile AppDatabase INSTANCE;

    // AppDatabase의 싱글톤 인스턴스를 가져오는 메서드
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
