package com.example.ble;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ble.Helper.DataHelper;
import com.example.ble.Helper.MsgHelper;
import com.example.ble.Helper.ReadHelper;

public class DataActivity extends AppCompatActivity implements ReadHelper.SensorDataCallback {
    private ReadHelper readHelper;
    private TextView sensorDataTextView;
    private TextView connectionStateTextView;
    private Button readButton;
    private Button stopButton;
    private String deviceName;
    private String deviceAddress;
    private String gender;
    private DataHelper dataHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        deviceName = getIntent().getStringExtra("DEVICE_NAME");
        deviceAddress = getIntent().getStringExtra("DEVICE_ADDRESS");
        gender = getIntent().getStringExtra("GENDER");

        TextView deviceNameTextView = findViewById(R.id.device_name);
        TextView deviceAddressTextView = findViewById(R.id.device_address);
        TextView genderTextView = findViewById(R.id.gender);
        sensorDataTextView = findViewById(R.id.sensor_data);
        connectionStateTextView = findViewById(R.id.connection_state);
        readButton = findViewById(R.id.read_button);
        stopButton = findViewById(R.id.stop_button);

        deviceNameTextView.setText(deviceName);
        deviceAddressTextView.setText(deviceAddress);
        genderTextView.setText(gender);

        readHelper = new ReadHelper(this, this);
        dataHelper = new DataHelper(this, deviceAddress, gender, readHelper);
        BluetoothDevice device = readHelper.getBluetoothAdapter().getRemoteDevice(deviceAddress);
        readHelper.connectToDevice(this, device);

        readButton.setOnClickListener(v -> dataHelper.startLearning());
        stopButton.setOnClickListener(v -> dataHelper.stopLearning());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        readHelper.disconnect();
        dataHelper.close();
    }

    @Override
    public void onSensorDataReceived(int[] sensorData) {
        MsgHelper.showLog("받은 바이트 여기서 다시 받음: " + java.util.Arrays.toString(sensorData));
        runOnUiThread(() -> {
            // 데이터베이스에 저장
            dataHelper.saveSensorData(sensorData);

            // 학습한 데이터 수를 업데이트
            int sensingCount = dataHelper.getSensingCount();
            sensorDataTextView.setText("학습한 데이터 수: " + sensingCount);
        });
    }

    @Override
    public void onConnectionStateChange(String stateMessage) {
        runOnUiThread(() -> connectionStateTextView.setText(stateMessage));
        MsgHelper.showLog("연결 상태 확인: " + stateMessage);
    }
}
