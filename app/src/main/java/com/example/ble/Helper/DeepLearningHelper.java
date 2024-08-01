package com.example.ble.Helper;

import android.content.Context;
import android.util.Log;

import com.example.ble.data.AppDatabase;
import com.example.ble.data.SensingData;
import com.example.ble.data.SensingDataDao;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class DeepLearningHelper {

    private static final String TAG = "DeepLearningHelper";
    private static final String MODEL_FILE = "model.tflite";
    private Interpreter tflite;
    private SensingDataDao sensingDataDao;

    public DeepLearningHelper(Context context) {
        try {
            tflite = new Interpreter(loadModelFile(context));
        } catch (Exception e) {
            e.printStackTrace();
        }
        sensingDataDao = AppDatabase.getDatabase(context).sensingDataDao();
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(context.getAssets().openFd(MODEL_FILE).getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = context.getAssets().openFd(MODEL_FILE).getStartOffset();
        long declaredLength = context.getAssets().openFd(MODEL_FILE).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void trainModel(Context context) {
        List<float[]> csvData = CSVUtils.readCSV(context, "sensor_data06.csv");
        List<SensingData> dbData = sensingDataDao.getAllSensingData();

        // CSV 데이터와 DB 데이터를 결합
        List<float[]> allData = new ArrayList<>(csvData);
        for (SensingData data : dbData) {
            allData.add(new float[]{
                    data.getMiddle_flex_sensor(),
                    data.getMiddle_pressure_sensor(),
                    data.getRing_flex_sensor(),
                    data.getRing_pressure_sensor(),
                    data.getPinky_flex_sensor()
            });
        }

        // TensorFlow Lite 모델 학습 로직 추가 (예시로 단순 로그 출력)
        for (float[] data : allData) {
            Log.d(TAG, "Training data: " + java.util.Arrays.toString(data));
        }
    }
}
