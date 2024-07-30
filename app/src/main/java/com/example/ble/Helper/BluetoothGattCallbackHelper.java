package com.example.ble.Helper;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.example.ble.Helper.MessageHelper;

public class BluetoothGattCallbackHelper extends BluetoothGattCallback {

    private Context context;

    public BluetoothGattCallbackHelper(Context context) {
        this.context = context;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothGatt.STATE_CONNECTED) {
            gatt.discoverServices();
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            MessageHelper.showToast(context, "연결이 끊어졌습니다.");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            MessageHelper.showToast(context, "서비스를 발견했습니다.");
        } else {
            MessageHelper.showToast(context, "서비스 발견에 실패했습니다.");
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            String value = new String(characteristic.getValue());
            MessageHelper.showToast(context, "Read: " + value);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            MessageHelper.showToast(context, "쓰기 성공");
        } else {
            MessageHelper.showToast(context, "쓰기 실패");
        }
    }
}
