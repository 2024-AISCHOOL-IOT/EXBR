package com.example.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ble.Helper.MsgHelper;
import com.example.ble.Helper.ReadHelper;
import com.example.ble.Helper.ScanHelper;

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity {
    private ScanHelper scanHelper;
    private ReadHelper readHelper;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        listView = findViewById(R.id.device_list);
        deviceList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        listView.setAdapter(adapter);

        scanHelper = new ScanHelper(this, new ScanHelper.ScanCallbackHandler() {
            @Override
            public void onScanResult(ScanResult result) {
                try {
                    String deviceInfo = result.getDevice().getName() + " - " + result.getDevice().getAddress();
                    if (!deviceList.contains(deviceInfo)) {
                        deviceList.add(deviceInfo);
                        adapter.notifyDataSetChanged();
                    }
                } catch (SecurityException e) {
                    MsgHelper.showLog("표시 실패");
                }
            }

            @Override
            public void onScanFinished(List<ScanResult> results) {
                MsgHelper.showToast(ScanActivity.this, "스캔이 완료되었습니다.");
            }
        });

        readHelper = new ReadHelper(this, new ReadHelper.SensorDataCallback() {
            @Override
            public void onSensorDataReceived(int[] sensorData) {
                // 데이터가 받아질 때 처리
                readHelper.saveSensorData(sensorData);
            }

            @Override
            public void onConnectionStateChange(String stateMessage) {
                MsgHelper.showToast(ScanActivity.this, stateMessage);
                if (stateMessage.contains("연결됨")) {
                    navigateToSelectActivity(readHelper.getConnectedDeviceName(), readHelper.getConnectedDeviceAddress());
                }
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = deviceList.get(position);
            String deviceAddress = deviceInfo.split(" - ")[1];
            BluetoothDevice device = readHelper.getBluetoothAdapter().getRemoteDevice(deviceAddress);
            readHelper.connectToDevice(this, device);
        });

        Button rescanButton = findViewById(R.id.rescan_button);
        rescanButton.setOnClickListener(v -> {
            deviceList.clear();
            adapter.notifyDataSetChanged();
            scanHelper.rescan();
        });

        // 초기 스캔 시작
        scanHelper.startScan();
    }

    private void navigateToSelectActivity(String deviceName, String deviceAddress) {
        Intent intent = new Intent(ScanActivity.this, SelectActivity.class);
        intent.putExtra("DEVICE_NAME", deviceName);
        intent.putExtra("DEVICE_ADDRESS", deviceAddress);
        startActivity(intent);
    }
}
