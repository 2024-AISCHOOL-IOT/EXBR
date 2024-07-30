package com.example.ble.Helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.ParcelUuid;

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

    public interface ScanHelperCallback {
        void onDeviceFound(BluetoothDevice device);
        void onScanFinished();
    }

    public ScanHelper(Context context, ScanHelperCallback callback) {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        this.handler = new Handler();
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
                MessageHelper.showLog("스캔실패");
            }
        };

        handler.postDelayed(this::stopScan, SCAN_PERIOD);
        try {
            bluetoothLeScanner.startScan(filters, settings, scanCallback);
        }catch (SecurityException e){
            MessageHelper.showLog("스캔실패");
        }

    }

    public void stopScan() {
        try {
            bluetoothLeScanner.stopScan(scanCallback);
            callback.onScanFinished();
        }catch (SecurityException e){
            MessageHelper.showLog("스캔실패");
        }

    }

    public void reset() {
        deviceList.clear();
    }
}
