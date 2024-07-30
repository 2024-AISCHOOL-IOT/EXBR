package com.example.ble.Helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class MessageHelper {
    // 토스트 메시지를 표시하는 헬퍼 메서드
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    // 로그 메시지를 표시하는 헬퍼 메서드
    public static void showLog(String message) {
        Log.d("로그",message);
    }
}
