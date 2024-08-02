package com.example.ble;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.ble.Helper.BleServiceHelper;
import com.example.ble.data.AppDatabase;
import com.example.ble.data.SensingData;
import com.example.ble.data.SensingDataDao;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DataActivity extends AppCompatActivity {
    private static final String TAG = "로그";

    private BleServiceHelper bluetoothService;
    private boolean isBound = false;
    private TextView sensorDataTextView;
    private TextView connectionStateTextView;
    private String deviceName;
    private String deviceAddress;
    private String gender;
    private String status;

    private final List<SensingData> sensingDataList = new ArrayList<>();
    private int totalDataCount = 0;
    private AppDatabase database;
    private SensingDataDao sensingDataDao;

    private final Object lock = new Object();  // 동기화를 위한 객체
    private final Handler mainHandler = new Handler(Looper.getMainLooper());  // 재사용 가능한 Handler
    private Thread backgroundThread;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // 백그라운드 스레드 초기화
        initializeBackgroundThread();

        deviceName = getIntent().getStringExtra("deviceName");
        deviceAddress = getIntent().getStringExtra("deviceAddress");
        gender = getIntent().getStringExtra("gender");

        TextView deviceNameTextView = findViewById(R.id.device_name);
        TextView deviceAddressTextView = findViewById(R.id.device_address);
        TextView genderTextView = findViewById(R.id.gender);
        sensorDataTextView = findViewById(R.id.sensor_data);
        connectionStateTextView = findViewById(R.id.connection_state);
        Button startButton = findViewById(R.id.start_button);
        Button stopButton = findViewById(R.id.stop_button);

        Log.d(TAG, "onCreate: deviceName=" + deviceName + ", deviceAddress=" + deviceAddress + ", gender=" + gender);

        deviceNameTextView.setText(deviceName);
        deviceAddressTextView.setText(deviceAddress);
        genderTextView.setText(gender);

        // 셀렉트 엑티비티에서 받아온 기기 이름 출력
        status = "연결 상태: 연결됨 (" + deviceName + ")";
        connectionStateTextView.setText(status);

        // 데이터베이스 초기화
        initializeDatabase();

        Intent intent = new Intent(this, BleServiceHelper.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        // 람다 표현식으로 변경된 OnClickListener
        startButton.setOnClickListener(v -> {
            stopLearning();
            handleStartButtonClick();
        });

        stopButton.setOnClickListener(v -> stopLearning());
    }

    private void initializeBackgroundThread() {
        if (backgroundThread != null && backgroundThread.isAlive()) {
            backgroundThread.interrupt();
        }
        backgroundThread = new Thread(() -> {
            Log.d(TAG, "Background thread initialized");
            // 백그라운드 스레드에서 수행할 초기화 작업이 있을 경우 여기에 추가
        });
        backgroundThread.start();
    }

    private void initializeDatabase() {
        File dbFile = getApplicationContext().getDatabasePath("app_database");
        if (!dbFile.exists()) {
            try {
                // Room 데이터베이스 인스턴스 생성 및 초기화
                database = AppDatabase.getDatabase(this);
                sensingDataDao = database.sensingDataDao();
                Log.d(TAG, "데이터베이스 초기화 완료");
            } catch (Exception e) {
                Log.e(TAG, "데이터베이스 초기화 오류: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "데이터베이스가 이미 존재합니다. 초기화를 건너뜁니다.");
            // 데이터베이스 인스턴스 생성
            database = AppDatabase.getDatabase(this);
            sensingDataDao = database.sensingDataDao();
        }
    }

    private void handleStartButtonClick() {
        initializeDatabase();
        initializeBackgroundThread();
        Log.d(TAG, "학습 시작 버튼 클릭됨");
        if (isBound && bluetoothService != null) {
            Log.d(TAG, "센싱 데이터 받아오기 호출");
            bluetoothService.startReceivingData(deviceAddress);
        } else {
            Log.d(TAG, "서비스가 아직 바인딩되지 않았습니다.");
            // 서비스 바인딩 재시도
            Intent intent = new Intent(this, BleServiceHelper.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void stopLearning() {
        if (isBound && bluetoothService != null) {
            bluetoothService.stopReceivingData();
            clearSensingDataTable(); // 데이터베이스 초기화
            Log.d(TAG, "학습 중지 버튼 클릭됨");
        } else {
            Log.d(TAG, "서비스가 아직 바인딩되지 않았습니다.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        if (backgroundThread != null && backgroundThread.isAlive()) {
            backgroundThread.interrupt();
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BleServiceHelper.LocalBinder binder = (BleServiceHelper.LocalBinder) service;
            bluetoothService = binder.getService();
            isBound = true;

            Log.d(TAG, "서비스 바인딩 완료");

            // 서비스가 바인딩되었을 때 초기화 메서드 호출
            bluetoothService.initialize(DataActivity.this, new BleServiceHelper.ConnectionCallback() {
                @Override
                public void onConnected(String deviceName, String deviceAddress) {
                    Log.d(TAG, "연결 성공: " + deviceName + " (" + deviceAddress + ")");
                }

                @Override
                public void onConnectionFailed() {
                    Log.d(TAG, "연결 실패");
                }

                @Override
                public void onDisconnected() {
                    Log.d(TAG, "연결 해제");
                }
            });

            bluetoothService.setDataReceivedListener(data -> new Thread(() -> handleDataReceived(data)).start());

            // 연결 상태를 갱신
            connectionStateTextView.setText(status);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
            isBound = false;

            Log.d(TAG, "서비스 연결 해제");

            // 연결 해제 상태를 갱신
            connectionStateTextView.setText("연결 상태: 해제됨");
        }
    };

    private void handleDataReceived(int[] data) {
        synchronized (lock) {
            // 데이터 수신 처리
            if (data.length == 5) {
                SensingData sensingData = new SensingData(
                        data[0], data[1], data[2], data[3], data[4], new Timestamp(System.currentTimeMillis())
                );
                sensingDataList.add(sensingData);

                // 40개의 데이터가 쌓이면 데이터베이스에 저장
                if (sensingDataList.size() >= 40) {
                    new Thread(this::saveDataAndClearList).start();
                }

                // 총 데이터 개수가 500개가 되면 데이터 수집 중지
                totalDataCount += 1;
                if (totalDataCount >= 500) {
                    // 남아있는 데이터를 모두 저장
                    new Thread(() -> {
                        if (!sensingDataList.isEmpty()) {
                            saveDataAndClearList();
                        }
                        // 데이터 수집 중지
                        bluetoothService.stopReceivingData();

                        // 딥러닝 액티비티로 이동
                        Intent intent = new Intent(DataActivity.this, DeepLearningActivity.class);
                        intent.putExtra("deviceName", deviceName);
                        intent.putExtra("deviceAddress", deviceAddress);
                        intent.putExtra("gender", gender);
                        startActivity(intent);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        finish();
                    }).start();
                }

                // 수신된 데이터를 화면에 표시
                runOnUiThread(() -> {
                    String displayText = getString(R.string.now_cnt) + totalDataCount + getString(R.string.end_cnt);
                    sensorDataTextView.setText(displayText);
                });
            }
        }
    }

    private void clearSensingDataTable() {
        new Thread(() -> {
            sensingDataDao.clear();
            sensingDataDao.resetPrimaryKey();
            SupportSQLiteDatabase db = database.getOpenHelper().getWritableDatabase();
            db.execSQL("VACUUM");
            totalDataCount = 0;
        }).start();
    }

    private void saveDataAndClearList() {
        try {
            sensingDataDao.insertAll(sensingDataList);
            sensingDataList.clear();
            mainHandler.postDelayed(() -> {}, 50);
        } catch (Exception e) {
            Log.e(TAG, "Error in saveDataAndClearList", e);
        }
    }
}
