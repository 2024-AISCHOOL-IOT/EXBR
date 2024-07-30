package com.example.ble.Helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;


import com.example.ble.LearnActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScanHelper {

    private static final long SCAN_PERIOD = 10000; // 10 seconds
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private Handler handler;
    private List<BluetoothDevice> deviceList;
    private ScanHelperCallback callback;
    private Context context;
    private BluetoothGatt bluetoothGatt; // 추가된 부분

    public interface ScanHelperCallback {
        void onDeviceFound(BluetoothDevice device);
        void onScanFinished();
    }

    public ScanHelper(Context context, ScanHelperCallback callback) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        this.handler = new Handler(Looper.getMainLooper());
        this.deviceList = new ArrayList<>();
        this.callback = callback;
    }

    public void startScan() {
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        reset();
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if (!deviceList.contains(device)) {
                    deviceList.add(device);
                    callback.onDeviceFound(device);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    BluetoothDevice device = result.getDevice();
                    if (!deviceList.contains(device)) {
                        deviceList.add(device);
                        callback.onDeviceFound(device);
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                handler.post(() -> MessageHelper.showLog("스캔 실패"));
            }
        };

        handler.postDelayed(this::stopScan, SCAN_PERIOD);
        try {
            bluetoothLeScanner.startScan(filters, settings, scanCallback);
        } catch (SecurityException e) {
            handler.post(() -> MessageHelper.showLog("스캔 실패"));
        }
    }

    public void stopScan() {
        try {
            bluetoothLeScanner.stopScan(scanCallback);
            handler.post(callback::onScanFinished);
        } catch (SecurityException e) {
            handler.post(() -> MessageHelper.showLog("스캔 끝"));
        }
    }

    public void reset() {
        deviceList.clear();
    }

    public void connectToDevice(Context context, BluetoothDevice device) {
        try {
            BluetoothGattCallbackHelper gattCallbackHelper = new BluetoothGattCallbackHelper(context, SensingHelper.getInstance(context), device.getAddress(), this);
            bluetoothGatt = device.connectGatt(context, false, gattCallbackHelper); // bluetoothGatt 변수 사용
        } catch (SecurityException e) {
            handler.post(() -> MessageHelper.showToast(context, "연결 실패"));
        }
    }

    public void onServicesDiscovered(String deviceMac) {
        // UI 스레드에서 Intent 시작
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(context, LearnActivity.class);
            intent.putExtra("device_mac", deviceMac); // MAC 주소 전달
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        });
    }

    public void closeConnection() {
        if (bluetoothGatt != null) {
            try {
                bluetoothGatt.close();
                bluetoothGatt = null;
            } catch (SecurityException e) {
                bluetoothGatt = null;
            }
        }
    }
}
