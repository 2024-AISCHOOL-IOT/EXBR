package com.example.ble;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ble.Helper.MsgHelper;

public class SelectActivity extends AppCompatActivity {
    private String deviceName;
    private String deviceAddress;
    private String gender;
    private boolean genderSelected = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        deviceName = getIntent().getStringExtra("DEVICE_NAME");
        deviceAddress = getIntent().getStringExtra("DEVICE_ADDRESS");

        TextView deviceNameTextView = findViewById(R.id.device_name);
        TextView deviceAddressTextView = findViewById(R.id.device_address);
        RadioGroup genderGroup = findViewById(R.id.gender_group);
        RadioButton maleButton = findViewById(R.id.male_button);
        RadioButton femaleButton = findViewById(R.id.female_button);
        Button learnButton = findViewById(R.id.learn_button);

        deviceNameTextView.setText(deviceName);
        deviceAddressTextView.setText(deviceAddress);

        genderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!genderSelected) {
                genderSelected = true;
                handler.postDelayed(() -> {
                    if (checkedId == R.id.male_button) {
                        gender = "Male";
                    } else if (checkedId == R.id.female_button) {
                        gender = "Female";
                    }
                    genderSelected = false;
                }, 500); // 0.5초 딜레이
            }
        });

        learnButton.setOnClickListener(v -> {
            if (gender == null || gender.isEmpty()) {
                MsgHelper.showToast(SelectActivity.this, "성별을 선택해 주세요.");
            } else {
                Intent intent = new Intent(SelectActivity.this, DataActivity.class);
                intent.putExtra("DEVICE_NAME", deviceName);
                intent.putExtra("DEVICE_ADDRESS", deviceAddress);
                intent.putExtra("GENDER", gender);
                startActivity(intent);
            }
        });
    }
}
