package com.terramaster.utils;

import android.util.Log;

public class LogM {
    public static void e(String message) {
        Log.e("log_tag", message);
    }

    public static void w(String message) {
        Log.w("log_tag", message);
    }

    public static void i(String message) {
        Log.i("log_tag", message);
    }

    public static void v(String message) {
        Log.v("log_tag", message);
    }
}
