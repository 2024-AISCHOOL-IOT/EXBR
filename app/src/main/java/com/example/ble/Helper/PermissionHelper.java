package com.example.ble.Helper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
    public static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;

    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied();
    }

    // 필요한 모든 권한을 확인하고 요청하는 메서드
    public static void checkAndRequestPermissions(Activity activity, PermissionCallback callback) {
        String[] permissions = getRequiredPermissions();
        List<String> permissionsNeeded = getUngrantedPermissions(activity, permissions);

        if (permissionsNeeded.isEmpty()) {
            callback.onPermissionsGranted();
        } else {
            ActivityCompat.requestPermissions(activity, permissionsNeeded.toArray(new String[0]), REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }

    // 필요하지 않은 권한 확인
    private static List<String> getUngrantedPermissions(Activity activity, String[] permissions) {
        List<String> permissionsNeeded = new ArrayList<>();
        // 권한이 있는지 확인하고 없으면 추가함
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }
        return permissionsNeeded;
    }

    // 모든 블루투스 관련 권한이 부여되었는지 확인
    public static boolean checkPermissions(Context context) {
        String[] permissions = getRequiredPermissions();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // SDK에 따라 필요한 권한 확인
    private static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        } else {
            return new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
    }

    // 권한 요청을 위한 다이얼로그를 표시하는 메서드
    public static void showPermissionRationale(Activity activity, PermissionCallback callback) {
        new AlertDialog.Builder(activity)
                .setTitle("권한 요청")
                .setMessage("어플을 사용하기 위해서는 블루투스 권한이 필요합니다. 권한을 허용해주세요.")
                .setPositiveButton("허용", (dialog, which) -> checkAndRequestPermissions(activity, callback))
                .setNegativeButton("거부", (dialog, which) -> callback.onPermissionsDenied())
                .show();
    }
}
