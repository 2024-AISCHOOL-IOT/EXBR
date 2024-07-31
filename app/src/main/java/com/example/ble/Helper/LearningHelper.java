package com.example.ble.Helper;

import android.content.Context;

import com.example.ble.data.AppDatabase;
import com.example.ble.data.SensingData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

public class LearningHelper {
    private AppDatabase database;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Context context;

    public LearningHelper(Context context) {
        this.context = context;
        database = AppDatabase.getDatabase(context);
    }

    public void processSensingDataFromDB() {
        executorService.execute(() -> {
            // 데이터베이스에서 모든 센싱 데이터를 가져옴
            List<SensingData> sensingDataList = database.sensingDataDao().getAllSensingData();

            // 가져온 데이터를 배열로 저장
            int[][] dataArray = new int[sensingDataList.size()][5];
            for (int i = 0; i < sensingDataList.size(); i++) {
                SensingData data = sensingDataList.get(i);
                dataArray[i][0] = data.getMiddle_flex_sensor();
                dataArray[i][1] = data.getMiddle_pressure_sensor();
                dataArray[i][2] = data.getRing_flex_sensor();
                dataArray[i][3] = data.getRing_pressure_sensor();
                dataArray[i][4] = data.getPinky_flex_sensor();
            }

            // 처리한 데이터를 출력하거나 다른 작업 수행
            for (int i = 0; i < dataArray.length; i++) {
                System.out.println("Row " + i + ": " +
                        dataArray[i][0] + ", " +
                        dataArray[i][1] + ", " +
                        dataArray[i][2] + ", " +
                        dataArray[i][3] + ", " +
                        dataArray[i][4]);
            }
        });
    }

    public void close() {
        executorService.shutdown();
    }
}
