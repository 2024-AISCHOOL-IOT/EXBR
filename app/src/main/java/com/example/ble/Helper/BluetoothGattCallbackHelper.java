package com.example.ble.Helper;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.UUID;

public class BluetoothGattCallbackHelper extends BluetoothGattCallback {

    private Context context;
    private SensingHelper sensingHelper;
    private String deviceMac;
    private ScanHelper scanHelper;

    public BluetoothGattCallbackHelper(Context context, SensingHelper sensingHelper, String deviceMac, ScanHelper scanHelper) {
        this.context = context;
        this.sensingHelper = sensingHelper;
        this.deviceMac = deviceMac;
        this.scanHelper = scanHelper;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Handler handler = new Handler(Looper.getMainLooper());
        if (newState == BluetoothGatt.STATE_CONNECTED) {
            try {
                gatt.discoverServices();
                handler.post(() -> MessageHelper.showToast(context, "디바이스에 연결되었습니다."));
            } catch (SecurityException e) {
                handler.post(() -> MessageHelper.showToast(context, "디바이스 연결에 실패했습니다."));
            }
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            handler.post(() -> MessageHelper.showToast(context, "디바이스 연결이 끊어졌습니다."));
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Handler handler = new Handler(Looper.getMainLooper());
        if (status == BluetoothGatt.GATT_SUCCESS) {
            BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"))
                    .getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"));
            sensingHelper.setCharacteristic(characteristic);
            handler.post(() -> MessageHelper.showToast(context, "서비스를 발견했습니다."));
            scanHelper.onServicesDiscovered(deviceMac); // 서비스 발견 시 ScanHelper로 알림 및 MAC 주소 전달
        } else {
            handler.post(() -> MessageHelper.showToast(context, "서비스 발견에 실패했습니다."));
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            sensingHelper.onCharacteristicRead(characteristic);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        sensingHelper.onCharacteristicRead(characteristic);
    }
}
