package com.example.ble.Helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.example.ble.DeepLearningActivity;
import com.example.ble.data.AppDatabase;
import com.example.ble.data.SensingData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadHelper {
    private static final String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    private static final String CHAR_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    private static final int BATCH_SIZE = 40; // 40개씩 저장
    private BluetoothAdapterHelper bluetoothAdapterHelper;
    private BluetoothGatt bluetoothGatt;
    private SensorDataCallback sensorDataCallback;
    private Handler handler = new Handler();
    private AppDatabase database;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isReading = false;
    private int sensingCount = 0;
    private Context context;
    private String deviceAddress;
    private String gender;
    private List<SensingData> sensorDataBatch = new ArrayList<>();

    public interface SensorDataCallback {
        void onSensorDataReceived(int[] sensorData);
        void onConnectionStateChange(String stateMessage);
    }

    public ReadHelper(Context context, SensorDataCallback sensorDataCallback) {
        this.context = context;
        this.sensorDataCallback = sensorDataCallback;
        this.bluetoothAdapterHelper = new BluetoothAdapterHelper(context, new BluetoothAdapterHelper.ConnectionCallback() {
            @Override
            public void onConnected(String deviceName, String deviceAddress) {
                try {
                    ReadHelper.this.deviceAddress = deviceAddress;
                    sensorDataCallback.onConnectionStateChange("연결됨: " + deviceName + " (" + deviceAddress + ")");
                    bluetoothGatt.discoverServices();
                } catch (SecurityException e) {
                    MsgHelper.showLog("권한 오류로 연결 안됨");
                }
            }

            @Override
            public void onConnectionFailed() {
                sensorDataCallback.onConnectionStateChange("연결 실패");
            }

            @Override
            public void onDisconnected() {
                sensorDataCallback.onConnectionStateChange("연결 해제됨");
            }
        });
        database = AppDatabase.getDatabase(context);
    }

    public void connectToDevice(Context context, BluetoothDevice device) {
        try {
            bluetoothAdapterHelper.connectToDevice(context, device);
            bluetoothGatt = device.connectGatt(context, false, gattCallback);
        } catch (SecurityException e) {
            MsgHelper.showLog("기기 연결 시도");
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapterHelper.getBluetoothAdapter();
    }

    public void startLearning() {
        if (!isReading) {
            isReading = true;
            sensingCount = 0;
            clearSensingTable(); // 센싱 테이블 초기화
        }
    }

    public void stopLearning() {
        isReading = false;
        handler.removeCallbacks(readSensorDataRunnable); // 데이터 읽기 콜백 제거
        executorService.shutdown();
    }

    private void clearSensingTable() {
        executorService.execute(() -> {
            try {
                database.sensingDataDao().clear();
                MsgHelper.showLog("DB 초기화 완료");
                handler.post(readSensorDataRunnable); // 초기화 후 데이터 읽기 시작
            } catch (Exception e) {
                MsgHelper.showLog("DB 초기화 오류: " + e.getMessage());
            }
        });
    }

    private final Runnable readSensorDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (isReading) {
                readCharacteristic();
                handler.postDelayed(this, 50); // 0.05초(50ms) 딜레이 -> 초당 20회
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

        synchronized (sensorDataBatch) {
            sensorDataBatch.add(sensingData);
        }
        sensingCount++;

        if (sensorDataBatch.size() >= BATCH_SIZE) {
            saveDataToDatabase();
        }

        if (sensingCount >= 1000) {
            stopLearning();
            saveDataToDatabase(); // 남아있는 데이터를 DB에 저장
            startDeepLearningActivity();
        }
    }

    private void saveDataToDatabase() {
        synchronized (sensorDataBatch) {
            if (!sensorDataBatch.isEmpty()) {
                List<SensingData> batchToSave = new ArrayList<>(sensorDataBatch);
                sensorDataBatch.clear();
                executorService.execute(() -> {
                    try {
                        database.sensingDataDao().insertAll(batchToSave);
                        MsgHelper.showLog("데이터 저장 완료");
                    } catch (Exception e) {
                        MsgHelper.showLog("DB 저장 오류: " + e.getMessage());
                    }
                });
            }
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

    public void readCharacteristic() {
        try {
            if (bluetoothGatt != null) {
                BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID));
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(CHAR_UUID));
                    if (characteristic != null) {
                        bluetoothGatt.readCharacteristic(characteristic);
                        MsgHelper.showLog("아두이노에서 받아옴");
                    } else {
                        MsgHelper.showLog("특성을 찾을 수 없습니다.");
                    }
                } else {
                    MsgHelper.showLog("서비스를 찾을 수 없습니다.");
                }
            } else {
                MsgHelper.showLog("BluetoothGatt가 null입니다.");
            }
        } catch (SecurityException e) {
            MsgHelper.showLog("리드헬퍼 센서값 읽기");
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            try {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    sensorDataCallback.onConnectionStateChange("연결됨: " + gatt.getDevice().getName() + " (" + gatt.getDevice().getAddress() + ")");
                    bluetoothGatt.discoverServices();
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    sensorDataCallback.onConnectionStateChange("연결 해제됨");
                    disconnect();
                } else {
                    sensorDataCallback.onConnectionStateChange("연결 실패");
                }
            } catch (SecurityException e) {
                MsgHelper.showLog("리드헬퍼 연결 상태");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                    if (service != null) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(CHAR_UUID));
                        if (characteristic != null) {
                            bluetoothGatt.setCharacteristicNotification(characteristic, true);
                        }
                    }
                } else {
                    sensorDataCallback.onConnectionStateChange("서비스 발견 실패: " + status);
                }
            } catch (SecurityException e) {
                MsgHelper.showLog("리드헬퍼 서비스 발견");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            try {
                if (CHAR_UUID.equals(characteristic.getUuid().toString())) {
                    readCharacteristic();
                }
            } catch (SecurityException e) {
                MsgHelper.showLog("리드헬퍼 특성 변경 처리");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (CHAR_UUID.equals(characteristic.getUuid().toString())) {
                        MsgHelper.showLog("받은 16바이트 변환");
                        byte[] data = characteristic.getValue();
                        int[] sensorData = new int[5]; // 5개의 16비트 데이터
                        for (int i = 0; i < 5; i++) {
                            sensorData[i] = (data[2 * i] & 0xFF) | ((data[2 * i + 1] & 0xFF) << 8);
                        }
                        sensorDataCallback.onSensorDataReceived(sensorData);
                    }
                }
            } catch (SecurityException e) {
                MsgHelper.showLog("리드헬퍼 특성 읽기 처리");
            }
        }
    };

    public void disconnect() {
        bluetoothAdapterHelper.disconnect();
    }
}
