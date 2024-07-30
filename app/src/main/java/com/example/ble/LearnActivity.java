package com.example.ble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ble.Helper.BluetoothGattCallbackHelper;
import com.example.ble.Helper.ScanHelper;
import com.example.ble.Helper.SensingHelper;

public class LearnActivity extends AppCompatActivity {

    private SensingHelper sensingHelper;
    private String deviceMac;
    private ScanHelper scanHelper;
    private TextView sensorDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_learn);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sensingHelper = SensingHelper.getInstance(this);
        sensorDataView = findViewById(R.id.sensor_data_view);

        Intent intent = getIntent();
        deviceMac = intent.getStringExtra("device_mac");

        RadioGroup sexGroup = findViewById(R.id.sex_group);
        sexGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_male) {
                sensingHelper.setSelectedSex("남");
            } else if (checkedId == R.id.radio_female) {
                sensingHelper.setSelectedSex("여");
            }
        });

        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> sensingHelper.startSensing());

        Button resetButton = findViewById(R.id.reset_button);
        resetButton.setOnClickListener(v -> resetSensingData());

        scanHelper = new ScanHelper(this, new ScanHelper.ScanHelperCallback() {
            @Override
            public void onDeviceFound(android.bluetooth.BluetoothDevice device) {
                // 이 부분은 LearnActivity에서는 필요하지 않음
            }

            @Override
            public void onScanFinished() {
                // 이 부분은 LearnActivity에서는 필요하지 않음
            }
        });

        BluetoothGattCallbackHelper gattCallbackHelper = new BluetoothGattCallbackHelper(this, sensingHelper, deviceMac, scanHelper);
        scanHelper.connectToDevice(this, BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceMac));
    }

    private void resetSensingData() {
        sensingHelper.resetSensingData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanHelper.closeConnection();
        unregisterReceiver(sensorDataReceiver);
    }

    private final BroadcastReceiver sensorDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.ble.SENSOR_DATA".equals(intent.getAction())) {
                String deviceMac = intent.getStringExtra("device_mac");
                int middleFlexSensor = intent.getIntExtra("middle_flex_sensor", 0);
                int middlePressureSensor = intent.getIntExtra("middle_pressure_sensor", 0);
                int ringFlexSensor = intent.getIntExtra("ring_flex_sensor", 0);
                int ringPressureSensor = intent.getIntExtra("ring_pressure_sensor", 0);
                int pinkyFlexSensor = intent.getIntExtra("pinky_flex_sensor", 0);
                int acceleration = intent.getIntExtra("acceleration", 0);
                int gyroscope = intent.getIntExtra("gyroscope", 0);
                int magneticField = intent.getIntExtra("magnetic_field", 0);

                String sensorDataText = "Device MAC: " + deviceMac + "\n" +
                        "Middle Flex Sensor: " + middleFlexSensor + "\n" +
                        "Middle Pressure Sensor: " + middlePressureSensor + "\n" +
                        "Ring Flex Sensor: " + ringFlexSensor + "\n" +
                        "Ring Pressure Sensor: " + ringPressureSensor + "\n" +
                        "Pinky Flex Sensor: " + pinkyFlexSensor + "\n" +
                        "Acceleration: " + acceleration + "\n" +
                        "Gyroscope: " + gyroscope + "\n" +
                        "Magnetic Field: " + magneticField;

                sensorDataView.setText(sensorDataText);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.ble.SENSOR_DATA");
        registerReceiver(sensorDataReceiver, filter);
    }
}
