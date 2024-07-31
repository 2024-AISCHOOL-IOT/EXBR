package com.example.ble.Helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.UUID;

public class ReadHelper {
    private static final String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    private static final String CHAR_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";

    private BluetoothAdapterHelper bluetoothAdapterHelper;
    private BluetoothGatt bluetoothGatt;
    private SensorDataCallback sensorDataCallback;

    public interface SensorDataCallback {
        void onSensorDataReceived(int[] sensorData);
        void onConnectionStateChange(String stateMessage);
    }

    public ReadHelper(Context context, SensorDataCallback sensorDataCallback) {
        this.sensorDataCallback = sensorDataCallback;
        bluetoothAdapterHelper = new BluetoothAdapterHelper(context, new BluetoothAdapterHelper.ConnectionCallback() {
            @Override
            public void onConnected(String deviceName, String deviceAddress) {
                try {
                    sensorDataCallback.onConnectionStateChange("연결됨: " + deviceName + " (" + deviceAddress + ")");
                    bluetoothGatt.discoverServices();
                } catch (SecurityException e) {
                    MsgHelper.showLog("리드헬퍼 연결 상태일 때");
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
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapterHelper.getBluetoothAdapter();
    }

    public void connectToDevice(Context context, BluetoothDevice device) {
        try {
            bluetoothAdapterHelper.connectToDevice(context, device);
            bluetoothGatt = device.connectGatt(context, false, gattCallback);
        } catch (SecurityException e) {
            MsgHelper.showLog("리드헬퍼 기기 연결");
        }
    }

    public void readCharacteristic() {
        try {
            if (bluetoothGatt != null) {
                BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID));
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(CHAR_UUID));
                    if (characteristic != null) {
                        bluetoothGatt.readCharacteristic(characteristic);
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
            if (CHAR_UUID.equals(characteristic.getUuid().toString())) {
                byte[] data = characteristic.getValue();
                int[] sensorData = new int[8];
                for (int i = 0; i < 8; i++) {
                    sensorData[i] = (data[2 * i] & 0xFF) | ((data[2 * i + 1] & 0xFF) << 8);
                }
                sensorDataCallback.onSensorDataReceived(sensorData);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (CHAR_UUID.equals(characteristic.getUuid().toString())) {
                    byte[] data = characteristic.getValue();
                    int[] sensorData = new int[8];
                    for (int i = 0; i < 8; i++) {
                        sensorData[i] = (data[2 * i] & 0xFF) | ((data[2 * i + 1] & 0xFF) << 8);
                    }
                    sensorDataCallback.onSensorDataReceived(sensorData);
                }
            }
        }
    };

    public void disconnect() {
        try {
            if (bluetoothGatt != null) {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                bluetoothGatt = null;
            }
        } catch (SecurityException e) {
            MsgHelper.showLog("리드헬퍼 연결 해제");
        }
    }
}
