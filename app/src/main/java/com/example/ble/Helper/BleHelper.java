package com.example.ble.Helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import com.example.ble.Helper.PermissionHelper.PermissionCallback;

public class BleHelper {

    private static final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final ActivityResultLauncher<Intent> enableBluetoothLauncher;
    private final Activity activity;

    public BleHelper(Activity activity, ActivityResultLauncher<Intent> enableBluetoothLauncher) {
        this.activity = activity;
        this.enableBluetoothLauncher = enableBluetoothLauncher;
    }

    public static boolean bleCheckList(Context context) {
        return PermissionHelper.checkPermissions(context) && bluetoothAdapter.isEnabled();
    }

    // 권한 요청을 위한 다이얼로그를 표시하는 메서드
    public static void showPermissionRationale(Activity activity, PermissionCallback callback) {
        new AlertDialog.Builder(activity)
                .setTitle("권한 요청")
                .setMessage("어플을 사용하기 위해서는 블루투스 권한이 필요합니다. 권한을 허용해주세요.")
                .setPositiveButton("허용", (dialog, which) -> PermissionHelper.checkAndRequestPermissions(activity, callback))
                .setNegativeButton("거부", (dialog, which) -> {
                    // 권한이 거부된 경우 처리
                    callback.onPermissionsDenied();
                })
                .show();
    }

    public void requestEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothLauncher.launch(enableBluetoothIntent);
        } else {
            NavigationHelper.navigateToScan(activity);
        }
    }
}
