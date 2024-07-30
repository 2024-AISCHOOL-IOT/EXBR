package com.example.ble.Helper;

import android.app.Activity;
import android.content.Intent;

import com.example.ble.MainActivity;
import com.example.ble.ScanActivity;

public class NavigationHelper {
    public static void navigateToMain(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void navigateToScan(Activity activity) {
        Intent intent = new Intent(activity, ScanActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }
}
