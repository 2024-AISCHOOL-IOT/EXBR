package com.example.ble.Helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;

public class ScanHelper {
    private static final long SCAN_PERIOD = 10000; // 10초
    private static final String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private Handler handler;
    private boolean scanning = false;
    private List<ScanResult> scanResults;

    public interface ScanCallbackHandler {
        void onScanResult(ScanResult result);
        void onScanFinished(List<ScanResult> results);
    }

    private ScanCallbackHandler callbackHandler;

    public ScanHelper(Context context, ScanCallbackHandler callbackHandler) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        handler = new Handler(Looper.getMainLooper());
        this.callbackHandler = callbackHandler;
        scanResults = new ArrayList<>();

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (!scanResults.contains(result)) {
                    scanResults.add(result);
                    callbackHandler.onScanResult(result);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult result : results) {
                    if (!scanResults.contains(result)) {
                        scanResults.add(result);
                        callbackHandler.onScanResult(result);
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                // 에러 처리 로직 추가
            }
        };
    }

    public void startScan() {
        try {
            if (!scanning) {
                scanResults.clear();
                ScanFilter filter = new ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid.fromString(SERVICE_UUID))
                        .build();
                List<ScanFilter> filters = new ArrayList<>();
                filters.add(filter);

                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();

                bluetoothLeScanner.startScan(filters, settings, scanCallback);
                scanning = true;

                handler.postDelayed(this::stopScan, SCAN_PERIOD);
            }
        }catch (SecurityException e){
            MsgHelper.showLog("스캔 시작");
        }

    }

    public void stopScan() {
        try {
            if (scanning) {
                bluetoothLeScanner.stopScan(scanCallback);
                scanning = false;
                callbackHandler.onScanFinished(new ArrayList<>(scanResults));
            }
        }catch (SecurityException e){
            MsgHelper.showLog("스캔 정지");
        }

    }

    public void rescan() {
        if (scanning) {
            stopScan();
        }
        startScan();
    }
}
