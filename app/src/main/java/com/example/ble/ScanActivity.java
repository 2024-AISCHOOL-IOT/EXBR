package com.example.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ble.Helper.BluetoothAdapterHelper;
import com.example.ble.Helper.MsgHelper;
import com.example.ble.Helper.ScanHelper;

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity {
    private ScanHelper scanHelper;
    private BluetoothAdapterHelper bluetoothAdapterHelper;
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

                }catch (SecurityException e){
                    MsgHelper.showLog("표시실패");
                }

            }

            @Override
            public void onScanFinished(List<ScanResult> results) {
                MsgHelper.showToast(ScanActivity.this, "스캔이 완료되었습니다.");
            }
        });

        bluetoothAdapterHelper = new BluetoothAdapterHelper(this, new BluetoothAdapterHelper.ConnectionCallback() {
            @Override
            public void onConnected(String deviceName, String deviceAddress) {
                Intent intent = new Intent(ScanActivity.this, SelectActivity.class);
                intent.putExtra("DEVICE_NAME", deviceName);
                intent.putExtra("DEVICE_ADDRESS", deviceAddress);
                startActivity(intent);
            }

            @Override
            public void onConnectionFailed() {
                MsgHelper.showToast(ScanActivity.this, "기기 연결에 실패했습니다.");
            }

            @Override
            public void onDisconnected() {
                MsgHelper.showToast(ScanActivity.this, "기기 연결이 끊어졌습니다.");
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = deviceList.get(position);
            String deviceAddress = deviceInfo.split(" - ")[1];
            BluetoothDevice device = bluetoothAdapterHelper.getBluetoothAdapter().getRemoteDevice(deviceAddress);
            bluetoothAdapterHelper.connectToDevice(this, device);
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
}
