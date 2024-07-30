package com.example.ble.Helper;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class SensingHelper {
    private static SensingHelper instance;
    private Handler handler;
    private BluetoothGattCharacteristic characteristic;
    private String deviceMac; // MAC 주소를 저장할 변수
    private boolean isSensingActive = false; // 센싱 작업 상태 변수
    private Context context;

    private SensingHelper(Context context) {
        this.context = context.getApplicationContext();
        handler = new Handler(Looper.getMainLooper());
    }

    public static synchronized SensingHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SensingHelper(context);
        }
        return instance;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public void startSensing() {
        try {
            if (characteristic != null) {
                isSensingActive = true;
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                startReadingData(characteristic);
            }
        } catch (SecurityException e) {
            handler.post(() -> MessageHelper.showToast(context, "디바이스 연결에 실패했습니다."));
        }
    }

    private void startReadingData(BluetoothGattCharacteristic characteristic) {
        handler.post(() -> MessageHelper.showLog("기록시작"));
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isSensingActive) {
                    byte[] sensorData = readSensorDataFromCharacteristic(characteristic);
                    if (sensorData != null) {
                        processSensorData(sensorData);
                        isSensingActive = false; // 한번만 읽기
                    }
                }
            }
        });
    }

    private byte[] readSensorDataFromCharacteristic(BluetoothGattCharacteristic characteristic) {
        return characteristic.getValue();
    }

    private void processSensorData(byte[] sensorData) {
        handler.post(() -> MessageHelper.showLog("받아오기"));

        // 센서 데이터 처리
        int middleFlexSensor = (Byte.toUnsignedInt(sensorData[0]) << 8) | Byte.toUnsignedInt(sensorData[1]);
        int middlePressureSensor = (Byte.toUnsignedInt(sensorData[2]) << 8) | Byte.toUnsignedInt(sensorData[3]);
        int ringFlexSensor = (Byte.toUnsignedInt(sensorData[4]) << 8) | Byte.toUnsignedInt(sensorData[5]);
        int ringPressureSensor = (Byte.toUnsignedInt(sensorData[6]) << 8) | Byte.toUnsignedInt(sensorData[7]);
        int pinkyFlexSensor = (Byte.toUnsignedInt(sensorData[8]) << 8) | Byte.toUnsignedInt(sensorData[9]);
        int acceleration = (Byte.toUnsignedInt(sensorData[10]) << 8) | Byte.toUnsignedInt(sensorData[11]);
        int gyroscope = (Byte.toUnsignedInt(sensorData[12]) << 8) | Byte.toUnsignedInt(sensorData[13]);
        int magneticField = (Byte.toUnsignedInt(sensorData[14]) << 8) | Byte.toUnsignedInt(sensorData[15]);

        // 런 액티비티로 데이터 전달
        Intent intent = new Intent("com.example.ble.SENSOR_DATA");
        intent.putExtra("device_mac", deviceMac);
        intent.putExtra("middle_flex_sensor", middleFlexSensor);
        intent.putExtra("middle_pressure_sensor", middlePressureSensor);
        intent.putExtra("ring_flex_sensor", ringFlexSensor);
        intent.putExtra("ring_pressure_sensor", ringPressureSensor);
        intent.putExtra("pinky_flex_sensor", pinkyFlexSensor);
        intent.putExtra("acceleration", acceleration);
        intent.putExtra("gyroscope", gyroscope);
        intent.putExtra("magnetic_field", magneticField);
        context.sendBroadcast(intent);
    }

    public void stopSensing() {
        isSensingActive = false; // 센싱 작업 중지
    }
}
