package com.example.ble.Helper;

import android.content.Context;

import com.example.ble.DB.AppDatabase;
import com.example.ble.DB.Entity.Sensing;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SqlHelper {

    private static SqlHelper instance;
    private AppDatabase database;
    private ExecutorService executorService;

    private SqlHelper(Context context) {
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized SqlHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SqlHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Sensing 관련 메서드
    public void insertSensing(Sensing sensing) {
        executorService.execute(() -> database.sensingDao().insert(sensing));
    }

    public void getAllSensings(OnDataFetchedListener<Sensing> listener) {
        executorService.execute(() -> {
            List<Sensing> data = database.sensingDao().getAll();
            listener.onFetched(data);
        });
    }

    public void deleteAllSensings() {
        executorService.execute(() -> database.sensingDao().deleteAll());
    }

    public void getSensingCount(OnDataCountListener listener) {
        executorService.execute(() -> {
            int count = database.sensingDao().getSensingCount();
            listener.onCountFetched(count);
        });
    }

    // 데이터 조회 콜백 인터페이스
    public interface OnDataFetchedListener<T> {
        void onFetched(List<T> data);
    }

    public interface OnDataCountListener {
        void onCountFetched(int count);
    }
}
