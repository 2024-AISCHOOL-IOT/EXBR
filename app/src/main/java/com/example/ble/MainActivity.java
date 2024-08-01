package com.example.ble;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ble.Helper.MsgHelper;
import com.example.ble.Helper.PermissionHelper;
import com.example.ble.data.AppDatabase;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private AppDatabase database;
    private final ActivityResultLauncher<Intent> enableBluetoothLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // 블루투스 활성화 성공 시 스캔 화면으로 이동
                    navigateToScan();
                } else {
                    // 블루투스 활성화 실패 시 토스트 메시지 표시
                    MsgHelper.showToast(MainActivity.this, "블루투스가 활성화되지 않았습니다.\n어플 사용을 위해 블루투스를 활성화 해주세요.");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // EdgeToEdge.enable(this); // 필요에 따라 활성화

        setContentView(R.layout.activity_main);

        // 데이터베이스 생성 및 초기화
        initializeDatabase();

        // 시작 버튼 클릭 리스너 설정
        Button startButton = findViewById(R.id.start_btn);
        startButton.setOnClickListener(v -> handleStartButtonClick());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeDatabase() {
        File dbFile = getApplicationContext().getDatabasePath("app_database");
        if (!dbFile.exists()) {
            try {
                // Room 데이터베이스 인스턴스 생성 및 초기화
                database = AppDatabase.getDatabase(this);
                MsgHelper.showLog("데이터베이스 초기화 완료");
            } catch (Exception e) {
                MsgHelper.showLog("데이터베이스 초기화 오류: " + e.getMessage());
            }
        } else {
            MsgHelper.showLog("데이터베이스가 이미 존재합니다. 초기화를 건너뜁니다.");
            // 데이터베이스 인스턴스 생성
            database = AppDatabase.getDatabase(this);
        }
    }

    // 버튼 클릭 시 발생 이벤트
    private void handleStartButtonClick() {
        // 블루투스 지원 여부 확인
        if (!PermissionHelper.isBluetoothSupported()) {
            MsgHelper.showToast(this, "블루투스를 지원하지 않는 기기 입니다.");
            return;
        }

        // 필요한 권한 확인 및 요청
        PermissionHelper.checkAndRequestPermissions(this, new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                // 블루투스 활성화 상태 확인
                if (!PermissionHelper.isBluetoothEnabled()) {
                    // 블루투스가 활성화되지 않은 경우 활성화 요청
                    PermissionHelper.requestEnableBluetooth(MainActivity.this, enableBluetoothLauncher);
                } else {
                    // 권한 및 블루투스 활성화 상태 확인 후 스캔 화면으로 이동
                    navigateToScan();
                }
            }

            @Override
            public void onPermissionsDenied() {
                // 권한이 거부된 경우 처리
                MsgHelper.showToast(MainActivity.this, "블루투스 권한이 필요합니다.\n권한을 허용해주세요.");
            }
        }, enableBluetoothLauncher);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults, new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                if (!PermissionHelper.isBluetoothEnabled()) {
                    PermissionHelper.requestEnableBluetooth(MainActivity.this, enableBluetoothLauncher);
                } else {
                    navigateToScan();
                }
            }

            @Override
            public void onPermissionsDenied() {
                MsgHelper.showToast(MainActivity.this, "블루투스 권한이 필요합니다.\n권한을 허용해주세요.");
            }
        }, this, enableBluetoothLauncher);
    }

    private void navigateToScan() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        MsgHelper.showLog("스캔 액티비티로 이동");
        startActivity(intent);
        finish();
    }
}
