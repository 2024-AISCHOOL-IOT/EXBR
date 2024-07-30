package com.example.ble.Helper;

import android.app.Activity;
import android.content.Intent;

import com.example.ble.DeepLearningActivity;
import com.example.ble.LearnActivity;
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

    public static void navigateToDeepLearning(Activity activity) {
        Intent intent = new Intent(activity, DeepLearningActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void navigateToLearn(Activity activity) {
        Intent intent = new Intent(activity, LearnActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }
}
