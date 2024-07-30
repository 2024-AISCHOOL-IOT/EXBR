package com.example.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ble.Helper.ScanHelper;
import com.example.ble.Helper.MessageHelper;

import java.util.ArrayList;

public class ScanActivity extends AppCompatActivity implements ScanHelper.ScanHelperCallback {

    private ScanHelper scanHelper;
    private ArrayAdapter<String> deviceListAdapter;
    private ArrayList<BluetoothDevice> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView devicesListView = findViewById(R.id.devices_list_view);
        Button refreshButton = findViewById(R.id.refresh_button);

        deviceList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        devicesListView.setAdapter(deviceListAdapter);

        scanHelper = new ScanHelper(this, this);

        refreshButton.setOnClickListener(v -> {
            scanHelper.stopScan();
            scanHelper.reset();
            deviceListAdapter.clear();
            scanHelper.startScan();
        });

        devicesListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = deviceList.get(position);
            scanHelper.connectToDevice(this, device);
        });

        scanHelper.startScan();
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        try {
            deviceList.add(device);
            deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
            deviceListAdapter.notifyDataSetChanged();
        } catch (SecurityException e) {
            MessageHelper.showToast(this, "권한 오류 메인 페이지로 이동");
            finish();
        }
    }

    @Override
    public void onScanFinished() {
        MessageHelper.showToast(this, "스캔이 완료되었습니다.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanHelper.closeConnection();
    }
}
