package com.example.ble.Helper;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.example.ble.DeepLearningActivity;
import com.example.ble.data.AppDatabase;
import com.example.ble.data.SensingData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataHelper {
    private AppDatabase database;
    private Handler handler = new Handler();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isReading = false;
    private int sensingCount = 0;
    private Context context;
    private String deviceAddress;
    private String gender;
    private ReadHelper readHelper;

    private List<SensingData> sensorDataBatch = new ArrayList<>();
    private static final int BATCH_SIZE = 20; // 배치 크기 설정
    private static final int SAVE_INTERVAL = 3000; // 3초마다 저장

    public DataHelper(Context context, String deviceAddress, String gender, ReadHelper readHelper) {
        this.context = context;
        this.deviceAddress = deviceAddress;
        this.gender = gender;
        this.readHelper = readHelper;
        database = AppDatabase.getDatabase(context);
    }

    public void startLearning() {
        if (!isReading) {
            isReading = true;
            clearSensingTable();
            handler.post(readSensorDataRunnable);
            handler.post(saveDataRunnable);
        }
    }

    public void stopLearning() {
        isReading = false;
        handler.removeCallbacks(readSensorDataRunnable);
        handler.removeCallbacks(saveDataRunnable);
        executorService.shutdown();
    }

    private void clearSensingTable() {
        executorService.execute(() -> database.sensingDataDao().clear());
        sensingCount = 0;
        sensorDataBatch.clear();
    }

    private Runnable readSensorDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (isReading) {
                readHelper.readCharacteristic();
                handler.postDelayed(this, 50); // 0.05초(50ms) 딜레이 -> 초당 20회
            }
        }
    };

    private Runnable saveDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (isReading) {
                saveDataToDatabase();
                handler.postDelayed(this, SAVE_INTERVAL);
            }
        }
    };

    public void saveSensorData(int[] sensorData) {
        SensingData sensingData = new SensingData(
                sensorData[0], // middle_flex_sensor
                sensorData[1], // middle_pressure_sensor
                sensorData[2], // ring_flex_sensor
                sensorData[3], // ring_pressure_sensor
                sensorData[4]  // pinky_flex_sensor
        );

        sensorDataBatch.add(sensingData);
        sensingCount++;

        if (sensingCount >= 1000) {
            stopLearning();
            saveDataToDatabase();
            startDeepLearningActivity();
        }
    }

    private void saveDataToDatabase() {
        if (!sensorDataBatch.isEmpty()) {
            executorService.execute(() -> {
                database.sensingDataDao().insertAll(sensorDataBatch);
                sensorDataBatch.clear();
            });
        }
    }

    public int getSensingCount() {
        return sensingCount;
    }

    private void startDeepLearningActivity() {
        Intent intent = new Intent(context, DeepLearningActivity.class);
        intent.putExtra("DEVICE_ADDRESS", deviceAddress);
        intent.putExtra("GENDER", gender);
        context.startActivity(intent);
    }

    public void close() {
        // Room 데이터베이스는 명시적으로 닫을 필요가 없음
    }
}
