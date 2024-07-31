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

    private List<ScanResult> scanResults = new ArrayList<>();

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

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                callbackHandler.onScanResult(result);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                for (ScanResult result : results) {
                    callbackHandler.onScanResult(result);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                // 에러 처리 로직 추가
            }
        };
    }

    public void startScan() {
        if (!scanning) {
            try {
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
            }catch (SecurityException e){
                MsgHelper.showLog("스캔시작오류");
            }

        }
    }

    public void stopScan() {
        if (scanning) {
            try {
                bluetoothLeScanner.stopScan(scanCallback);
                scanning = false;
                callbackHandler.onScanFinished(scanResults);
            }catch (SecurityException e){
                MsgHelper.showLog("스캔종료오류");
            }

        }
    }

    public void rescan() {
        if (scanning) {
            stopScan();
        }
        startScan();
    }
}
