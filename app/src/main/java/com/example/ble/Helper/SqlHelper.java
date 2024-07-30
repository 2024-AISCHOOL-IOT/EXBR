package com.example.ble.Helper;

import android.content.Context;

import com.example.ble.DB.AppDatabase;
import com.example.ble.DB.Entity.Assistance;
import com.example.ble.DB.Entity.Device;
import com.example.ble.DB.Entity.DeepLearning;
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

    // Assistance 관련 메서드
    public void insertAssistance(Assistance assistance) {
        executorService.execute(() -> database.assistanceDao().insert(assistance));
    }

    public void getAllAssistances(OnDataFetchedListener<Assistance> listener) {
        executorService.execute(() -> {
            List<Assistance> data = database.assistanceDao().getAll();
            listener.onFetched(data);
        });
    }

    // Device 관련 메서드
    public void insertDevice(Device device) {
        executorService.execute(() -> database.deviceDao().insert(device));
    }

    public void getAllDevices(OnDataFetchedListener<Device> listener) {
        executorService.execute(() -> {
            List<Device> data = database.deviceDao().getAll();
            listener.onFetched(data);
        });
    }

    // DeepLearning 관련 메서드
    public void insertDeepLearning(DeepLearning deepLearning) {
        executorService.execute(() -> database.deepLearningDao().insert(deepLearning));
    }

    public void getAllDeepLearnings(OnDataFetchedListener<DeepLearning> listener) {
        executorService.execute(() -> {
            List<DeepLearning> data = database.deepLearningDao().getAll();
            listener.onFetched(data);
        });
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

    // 데이터 조회 콜백 인터페이스
    public interface OnDataFetchedListener<T> {
        void onFetched(List<T> data);
    }
}
