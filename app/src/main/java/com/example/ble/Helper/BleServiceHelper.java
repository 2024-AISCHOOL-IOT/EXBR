package com.example.ble.Helper;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.ble.data.SensingDataDao;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class BleServiceHelper extends Service {
    private static final String TAG = "로그";
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID CHAR_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private ConnectionCallback connectionCallback;
    private DataReceivedListener dataReceivedListener;
    private final IBinder binder = new LocalBinder();
    private boolean receiveData = false; // 데이터 수신 여부 플래그

    public interface ConnectionCallback {
        void onConnected(String deviceName, String deviceAddress);
        void onConnectionFailed();
        void onDisconnected();
    }

    public interface DataReceivedListener {
        void onDataReceived(int[] data);
    }

    public class LocalBinder extends Binder {
        public BleServiceHelper getService() {
            return BleServiceHelper.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void initialize(Context context, ConnectionCallback callback) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.e(TAG, "initialize: BluetoothManager를 가져올 수 없습니다.");
            return;
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "initialize: BluetoothAdapter를 가져올 수 없습니다.");
            return;
        }
        connectionCallback = callback;
        Log.d(TAG, "initialize: BluetoothAdapter 초기화 완료");
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void connectToDevice(Context context, BluetoothDevice device) {
        try {
            Log.d(TAG, "connectToDevice: 장치 연결 시도 중");
            bluetoothGatt = device.connectGatt(context, false, gattCallback);
        } catch (SecurityException e) {
            Log.e(TAG, "Bluetooth 연결 실패", e);
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            try {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d(TAG, "onConnectionStateChange: 장치 연결됨");
                    gatt.discoverServices();
                    connectionCallback.onConnected(gatt.getDevice().getName(), gatt.getDevice().getAddress());
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.d(TAG, "onConnectionStateChange: 장치 연결 해제됨");
                    connectionCallback.onDisconnected();
                } else {
                    Log.d(TAG, "onConnectionStateChange: 연결 실패");
                    connectionCallback.onConnectionFailed();
                }
            } catch (SecurityException e) {
                Log.e(TAG, "onConnectionStateChange에서 오류 발생", e);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "onServicesDiscovered: 서비스 발견됨");
                    BluetoothGattService service = gatt.getService(SERVICE_UUID);
                    if (service != null) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHAR_UUID);
                        if (characteristic != null) {
                            gatt.setCharacteristicNotification(characteristic, true);
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                            if (descriptor != null) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                                Log.d(TAG, "onServicesDiscovered: 특성 알림 설정 완료");

                                // 데이터 수신 플래그가 설정된 경우에만 데이터 수신 로직 실행
                                if (receiveData) {
                                    setupDataReceiving(gatt, characteristic);
                                }
                            }
                        }
                    }
                }
            } catch (SecurityException e) {
                Log.e(TAG, "onServicesDiscovered에서 오류 발생", e);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (!receiveData) return; // 데이터 수신 플래그가 false인 경우 데이터 수신 중단

            try {
                if (CHAR_UUID.equals(characteristic.getUuid())) {
                    final byte[] data = characteristic.getValue();
                    Log.d(TAG, "onCharacteristicChanged: 센싱 데이터 받아옴");

                    // 바이트 배열을 16비트 정수 배열로 변환
                    int[] sensorData = new int[data.length / 2];
                    ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
                    for (int i = 0; i < sensorData.length; i++) {
                        sensorData[i] = byteBuffer.getShort() & 0xFFFF; // 16비트 무부호 정수로 변환
                    }

                    // 데이터 출력
                    StringBuilder dataString = new StringBuilder();
                    for (int value : sensorData) {
                        dataString.append(value).append(" ");
                    }
                    Log.d(TAG, "전환된 데이터: " + dataString);

                    if (dataReceivedListener != null) {
                        dataReceivedListener.onDataReceived(sensorData);
                    }
                    // Note: Do not disconnect immediately here; let the DataActivity handle disconnection if necessary
                }
            } catch (SecurityException e) {
                Log.e(TAG, "onCharacteristicChanged에서 오류 발생", e);
            }
        }
    };

    private void setupDataReceiving(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
        try {
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                Log.d(TAG, "setupDataReceiving: 데이터 수신 설정 완료");
            }
        } catch (SecurityException e) {
            Log.d(TAG, "setupDataReceiving: 데이터 수신 설정 실패");
        }
    }

    public void setDataReceivedListener(DataReceivedListener listener) {
        dataReceivedListener = listener;
    }

    public void startReceivingData(String deviceAddress) {
        receiveData = true;
        if (bluetoothAdapter == null) {
            Log.e(TAG, "startReceivingData: BluetoothAdapter가 초기화되지 않았습니다.");
            return;
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        if (device == null) {
            Log.e(TAG, "startReceivingData: 장치를 찾을 수 없습니다.");
            return;
        }
        Log.d(TAG, "startReceivingData: BLe헬퍼 센싱데이터 받아오기 호출");
        connectToDevice(this, device);
    }

    public void stopReceivingData() {
        receiveData = false; // 데이터 수신 플래그 설정
    }

    public void connectToDeviceWithoutReceivingData(String deviceAddress) {
        receiveData = false;
        if (bluetoothAdapter == null) {
            Log.e(TAG, "connectToDeviceWithoutReceivingData: BluetoothAdapter가 초기화되지 않았습니다.");
            return;
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        if (device == null) {
            Log.e(TAG, "connectToDeviceWithoutReceivingData: 장치를 찾을 수 없습니다.");
            return;
        }
        Log.d(TAG, "connectToDeviceWithoutReceivingData: 장치에 연결 시도 중");
        connectToDevice(this, device);
    }

    private void disconnect() {
        try {
            if (bluetoothGatt != null) {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
                bluetoothGatt = null;
                Log.d(TAG, "disconnect: 장치 연결 해제됨");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "disconnect에서 오류 발생", e);
        }
    }
}
