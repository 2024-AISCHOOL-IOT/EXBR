package com.example.ble;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ble.Helper.BleHelper;
import com.example.ble.Helper.PermissionHelper;
import com.example.ble.Helper.MessageHelper;
import com.example.ble.Helper.NavigationHelper;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BleHelper bleHelper;

    private final ActivityResultLauncher<Intent> enableBluetoothLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // 블루투스 활성화 성공 시 스캔 화면으로 이동
                    NavigationHelper.navigateToScan(MainActivity.this);
                } else {
                    // 블루투스 활성화 실패 시 토스트 메시지 표시
                    MessageHelper.showToast(MainActivity.this, "블루투스가 활성화되지 않았습니다.\n어플 사용을 위해 블루투스를 활성화 해주세요.");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        bleHelper = new BleHelper(this, enableBluetoothLauncher);

        // 시작 버튼 클릭 리스너 설정
        Button startButton = findViewById(R.id.start_btn);
        startButton.setOnClickListener(v -> handleStartButtonClick());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 버튼 클릭 시 발생 이벤트
    private void handleStartButtonClick() {
        if (bluetoothAdapter == null) {
            MessageHelper.showToast(MainActivity.this, "블루투스를 지원하지 않는 기기 입니다.");
            return; // 메서드 종료
        }

        boolean isBluetoothReady = BleHelper.bleCheckList(this);
        if (isBluetoothReady && bluetoothAdapter.isEnabled()) {
            NavigationHelper.navigateToScan(MainActivity.this);
        } else {
            checkPermissions();
        }
    }

    private void checkPermissions() {
        if (!PermissionHelper.checkPermissions(this)) {
            // 권한이 없는 경우 권한 요청
            MessageHelper.showToast(MainActivity.this, "어플을 사용하기 위한 권환이 없습니다.");
            PermissionHelper.checkAndRequestPermissions(this, new PermissionHelper.PermissionCallback() {
                @Override
                public void onPermissionsGranted() {
                    // 권한이 다 있는데 블루투스 비활성화면 활성화
                    bleHelper.requestEnableBluetooth();
                }

                @Override
                public void onPermissionsDenied() {
                    // 권한이 거부된 경우 다이얼로그 표시
                    BleHelper.showPermissionRationale(MainActivity.this, new PermissionHelper.PermissionCallback() {
                        @Override
                        public void onPermissionsGranted() {
                            // 권한이 허용된 경우 블루투스 활성화 요청
                            bleHelper.requestEnableBluetooth();
                        }

                        @Override
                        public void onPermissionsDenied() {
                            // 권한이 다시 거부된 경우 처리
                            MessageHelper.showToast(MainActivity.this, "블루투스 권한이 필요합니다.\n권한을 허용해주세요.");
                        }
                    });
                }
            });
        } else {
            // 권한이 있는 경우 블루투스 활성화 확인
            bleHelper.requestEnableBluetooth();
        }
    }
}
