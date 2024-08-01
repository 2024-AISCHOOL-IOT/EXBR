package com.example.ble;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ble.data.AppDatabase;
import com.example.ble.data.SensingData;
import com.example.ble.data.SensingDataDao;

import java.util.ArrayList;
import java.util.List;

public class DeepLearningActivity extends AppCompatActivity {

    private static final String TAG = "로그";

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private AppDatabase database;
    private SensingDataDao sensingDataDao;
    private Button checkDataButton;
    private List<String> sensingDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deep_learning);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.list_view);
        sensingDataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sensingDataList);
        listView.setAdapter(adapter);

        database = AppDatabase.getDatabase(this);
        sensingDataDao = database.sensingDataDao();

        checkDataButton = findViewById(R.id.check_data_button);
        checkDataButton.setOnClickListener(v -> loadSensingData());
    }

    private void loadSensingData() {
        new Thread(() -> {
            Log.d(TAG,"센싱데이타조회하기");
            List<SensingData> sensingData = sensingDataDao.getAllSensingData();
            runOnUiThread(() -> {
                sensingDataList.clear();
                for (SensingData data : sensingData) {
                    sensingDataList.add(data.toString());
                }
                adapter.notifyDataSetChanged();
            });
        }).start();
    }
}
