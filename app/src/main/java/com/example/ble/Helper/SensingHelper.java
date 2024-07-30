package com.example.ble.Helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.ble.DB.AppDatabase;
import com.example.ble.DB.Entity.Sensing;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SensingHelper {
    private static SensingHelper instance;
    private AppDatabase database;
    private ExecutorService executorService;
    private Handler handler;
    private String selectedSex = "남"; // 기본값 설정
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private String deviceMac; // MAC 주소를 저장할 변수
    private boolean isSensingActive = false; // 센싱 작업 상태 변수

    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    private SensingHelper(Context context) {
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());
    }

    public static synchronized SensingHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SensingHelper(context.getApplicationContext());
        }
        return instance;
    }

    public void connectToDevice(Context context, String deviceMac) {
        this.deviceMac = deviceMac; // MAC 주소 설정
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceMac);
            BluetoothGattCallbackHelper gattCallbackHelper = new BluetoothGattCallbackHelper(context);
            bluetoothGatt = device.connectGatt(context, false, gattCallbackHelper);
        } catch (SecurityException e){
            MessageHelper.showToast(context,"기기와 연결이 끊겼습니다.");
        }

    }

    public void startSensing() {
        if (bluetoothGatt != null) {
            BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
            if (service != null) {
                characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                isSensingActive = true;
                startReadingData(characteristic);
            }
        }
    }

    private void startReadingData(BluetoothGattCharacteristic characteristic) {
        handler.post(new Runnable() {
            private int sensingCount = 0;

            @Override
            public void run() {
                if (sensingCount < 6000 && isSensingActive) {
                    byte[] sensorData = readSensorDataFromCharacteristic(characteristic);
                    Sensing sensing = processSensorData(sensorData);
                    saveSensingData(sensing);
                    sensingCount++;
                    handler.postDelayed(this, 50); // 50ms 마다 실행 (초당 20번)
                }
            }
        });
    }

    private byte[] readSensorDataFromCharacteristic(BluetoothGattCharacteristic characteristic) {
        return characteristic.getValue();
    }

    private Sensing processSensorData(byte[] sensorData) {
        Sensing sensing = new Sensing();
        sensing.setDevice_mac(deviceMac); // 저장된 MAC 주소 사용
        sensing.setMiddle_flex_sensor(sensorData[0]);
        sensing.setMiddle_pressure_sensor(sensorData[1]);
        sensing.setRing_flex_sensor(sensorData[2]);
        sensing.setRing_pressure_sensor(sensorData[3]);
        sensing.setPinky_flex_sensor(sensorData[4]);
        sensing.setAcceleration(sensorData[5]);
        sensing.setGyroscope(sensorData[6]);
        sensing.setMagnetic_field(sensorData[7]);
        sensing.setCreated_at(new Timestamp(System.currentTimeMillis())); // Timestamp 설정
        return sensing;
    }

    private void saveSensingData(Sensing sensing) {
        executorService.execute(() -> database.sensingDao().insert(sensing));
    }

    public void resetSensingData() {
        isSensingActive = false; // 센싱 작업 중지
        executorService.execute(() -> database.sensingDao().deleteAll());
    }

    public void closeConnection() {
        if (bluetoothGatt != null) {
            try {
                bluetoothGatt.close();
                bluetoothGatt = null;
            }catch (SecurityException e){
                bluetoothGatt = null;
            }

        }
    }

    // selectedSex 값을 설정하는 메서드
    public void setSelectedSex(String sex) {
        this.selectedSex = sex;
    }

    // selectedSex 값을 가져오는 메서드
    public String getSelectedSex() {
        return selectedSex;
    }
}
