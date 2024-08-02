package com.example.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ble.Helper.BleServiceHelper;
import com.example.ble.Helper.MsgHelper;
import com.example.ble.Helper.PermissionHelper;
import com.example.ble.Helper.ScanHelper;

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity {
    private ScanHelper scanHelper;
    private ListView deviceListView;
    private Button rescanButton;
    private ArrayAdapter<String> deviceListAdapter;
    private List<String> deviceList;
    private List<ScanResult> scanResults;
    private BleServiceHelper bleService;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 블루투스 활성화를 요청하는 ActivityResultLauncher
    private final ActivityResultLauncher<Intent> enableBluetoothLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // 블루투스 활성화 성공 시 스캔 시작
                    scanHelper.startScan();
                } else {
                    // 블루투스 활성화 실패 시 토스트 메시지 표시
                    MsgHelper.showToast(ScanActivity.this, "블루투스가 활성화되지 않았습니다.\n어플 사용을 위해 블루투스를 활성화 해주세요.");
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        deviceListView = findViewById(R.id.device_list);
        rescanButton = findViewById(R.id.rescan_button);

        deviceList = new ArrayList<>();
        scanResults = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        deviceListView.setAdapter(deviceListAdapter);

        bleService = new BleServiceHelper();
        bleService.initialize(this, new BleServiceHelper.ConnectionCallback() {
            @Override
            public void onConnected(String deviceName, String deviceAddress) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(ScanActivity.this, SelectActivity.class);
                    intent.putExtra("deviceName", deviceName);
                    intent.putExtra("deviceAddress", deviceAddress);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onConnectionFailed() {
                // Handle connection failure
            }

            @Override
            public void onDisconnected() {
                // Handle disconnection
            }
        });

        scanHelper = new ScanHelper(this, new ScanHelper.ScanCallbackHandler() {
            @Override
            public void onScanResult(ScanResult result) {
                try {
                    runOnUiThread(() -> {
                        String deviceInfo = "Device: " + result.getDevice().getName() + " - " + result.getDevice().getAddress();
                        if (!deviceList.contains(deviceInfo)) {
                            deviceList.add(deviceInfo);
                            scanResults.add(result);
                            deviceListAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (SecurityException e) {
                    MsgHelper.showLog("리스트 표시 실패");
                }
            }

            @Override
            public void onScanFinished(List<ScanResult> results) {
                runOnUiThread(() -> {
                    MsgHelper.showToast(ScanActivity.this, "스캔 종료");
                    deviceListAdapter.notifyDataSetChanged();
                });
            }
        });

        rescanButton.setOnClickListener(v -> rescan());

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private boolean itemClicked = false; // 중복 클릭 방지 플래그

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!itemClicked && position < scanResults.size()) {
                    itemClicked = true; // 플래그 설정
                    ScanResult selectedResult = scanResults.get(position);
                    BluetoothDevice device = selectedResult.getDevice();
                    bleService.connectToDeviceWithoutReceivingData(device.getAddress()); // 데이터 수신 없이 연결

                    // 0.5초 후 중복 클릭 방지 플래그 해제
                    mainHandler.postDelayed(() -> itemClicked = false, 500);
                }
            }
        });

        // 권한 확인 및 요청
        PermissionHelper.checkAndRequestPermissions(this, new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                scanHelper.startScan();
            }

            @Override
            public void onPermissionsDenied() {
                MsgHelper.showToast(ScanActivity.this, "블루투스 권한이 필요합니다.\n권한을 허용해주세요.");
            }
        }, enableBluetoothLauncher);
    }

    private void rescan() {
        deviceList.clear();
        scanResults.clear();
        deviceListAdapter.notifyDataSetChanged();
        scanHelper.rescan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanHelper.stopScan();
    }
}
