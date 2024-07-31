package com.example.ble.Helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;

public class BluetoothAdapterHelper {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private ConnectionCallback connectionCallback;

    public interface ConnectionCallback {
        void onConnected(String deviceName, String deviceAddress);
        void onConnectionFailed();
        void onDisconnected();
    }

    public BluetoothAdapterHelper(Context context, ConnectionCallback connectionCallback) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        this.connectionCallback = connectionCallback;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void connectToDevice(Context context, BluetoothDevice device) {
        try {
            bluetoothGatt = device.connectGatt(context, false, gattCallback);
        } catch (SecurityException e) {
            MsgHelper.showLog("기기 연결 실패: " + e.getMessage());
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                try {
                    connectionCallback.onConnected(gatt.getDevice().getName(), gatt.getDevice().getAddress());
                } catch (SecurityException e) {
                    MsgHelper.showLog("콜백 실패: " + e.getMessage());
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                connectionCallback.onDisconnected();
            } else {
                connectionCallback.onConnectionFailed();
            }
        }

        // 필요시 다른 GATT 콜백 메서드 추가 가능
    };

    public void disconnect() {
        if (bluetoothGatt != null) {
            try {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
            } catch (SecurityException e) {
                MsgHelper.showLog("연결 해제 실패: " + e.getMessage());
            }

            bluetoothGatt = null;
        }
    }
}
