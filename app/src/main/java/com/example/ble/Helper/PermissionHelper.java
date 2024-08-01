package com.example.ble.Helper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
    public static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private static BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied();
    }

    // 필요한 모든 권한을 확인하고 요청하는 메서드
    public static void checkAndRequestPermissions(Activity activity, PermissionCallback callback, ActivityResultLauncher<Intent> enableBluetoothLauncher) {
        String[] permissions = getRequiredPermissions();
        List<String> permissionsNeeded = getUngrantedPermissions(activity, permissions);

        if (permissionsNeeded.isEmpty()) {
            if (bluetoothAdapter == null) {
                MsgHelper.showToast(activity, "블루투스를 지원하지 않는 기기 입니다.");
                callback.onPermissionsDenied();
            } else if (!bluetoothAdapter.isEnabled()) {
                requestEnableBluetooth(activity, enableBluetoothLauncher);
            } else {
                callback.onPermissionsGranted();
            }
        } else {
            boolean shouldShowRationale = false;
            for (String permission : permissionsNeeded) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    shouldShowRationale = true;
                    break;
                }
            }

            if (shouldShowRationale) {
                showPermissionRationale(activity, callback, enableBluetoothLauncher);
            } else {
                ActivityCompat.requestPermissions(activity, permissionsNeeded.toArray(new String[0]), REQUEST_BLUETOOTH_PERMISSIONS);
            }
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
    public static void showPermissionRationale(Activity activity, PermissionCallback callback, ActivityResultLauncher<Intent> enableBluetoothLauncher) {
        new AlertDialog.Builder(activity)
                .setTitle("권한 요청")
                .setMessage("어플을 사용하기 위해서는 블루투스 권한이 필요합니다. 권한을 허용해주세요.")
                .setPositiveButton("허용", (dialog, which) -> checkAndRequestPermissions(activity, callback, enableBluetoothLauncher))
                .setNegativeButton("거부", (dialog, which) -> callback.onPermissionsDenied())
                .show();
    }

    // 블루투스 활성화 요청 메서드
    public static void requestEnableBluetooth(Activity activity, ActivityResultLauncher<Intent> enableBluetoothLauncher) {
        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBluetoothLauncher.launch(enableBluetoothIntent);
    }

    // 블루투스 지원 여부 확인 메서드
    public static boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    // 블루투스 활성화 상태 확인 메서드
    public static boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    // 권한 요청 결과를 처리하는 메서드
    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, PermissionCallback callback, Activity activity, ActivityResultLauncher<Intent> enableBluetoothLauncher) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                callback.onPermissionsGranted();
            } else {
                showPermissionRationale(activity, callback, enableBluetoothLauncher);
            }
        }
    }
}
