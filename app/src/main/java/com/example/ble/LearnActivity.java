package com.example.ble;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ble.Helper.SensingHelper;

public class LearnActivity extends AppCompatActivity {

    private SensingHelper sensingHelper;
    private String deviceMac;

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

        sensingHelper.connectToDevice(this, deviceMac);
    }

    private void resetSensingData() {
        sensingHelper.resetSensingData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensingHelper.closeConnection();
    }
}
