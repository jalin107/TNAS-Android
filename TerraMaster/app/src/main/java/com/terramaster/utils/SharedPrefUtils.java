package com.terramaster.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.terramaster.discover.DeviceData;

public class SharedPrefUtils {
    private static SharedPrefUtils instance;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public SharedPrefUtils(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = mPreferences.edit();
    }

    public static SharedPrefUtils getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefUtils(context);
        }
        return instance;
    }

    public boolean isFirstTime() {
        return mPreferences.getBoolean("is_first_time", true);
    }

    public void setFirstTime(boolean b) {
        editor.putBoolean("is_first_time", b).commit();
    }

    public boolean isLogin() {
        return mPreferences.getBoolean("isLogin", false);
    }

    public String getLoginDeviceName() {
        return mPreferences.getString("ip", null);
    }

    public DeviceData getDeviceData() {
        String ip = mPreferences.getString("ip", "");
        int BCAST_PORT = mPreferences.getInt("BCAST_PORT", 0);
        String jsonData = mPreferences.getString("jsonData", "");
        if (ip.length() > 0 && jsonData.length() > 0) {
            try {
                return new DeviceData(BCAST_PORT, jsonData);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        //For testing purpose only...
        DeviceData deviceData = new DeviceData();
        deviceData.IPV4 = ip;
        deviceData.MAC = "1";
        deviceData.addDeviceService("ftp", "", 3721);
        return deviceData;
    }

    public String getUsername() {
        // TODO Auto-generated method stub
        return mPreferences.getString("username", "");
    }

    public String getSession(){
        return mPreferences.getString("PHPSESSID", "");
    }

    public String getPassword() {
        // TODO Auto-generated method stub
        return mPreferences.getString("password", "");
    }

    public void setDeviceData(DeviceData deviceData, String username, String password) {
        editor.putBoolean("isLogin", true).commit();
        editor.putString("ip", deviceData.IPV4).commit();
        editor.putInt("BCAST_PORT", deviceData.BCAST_PORT).commit();
        editor.putString("jsonData", deviceData.jsonData).commit();
        editor.putString("PHPSESSID", deviceData.PHPSESSID).commit();
        editor.putString("username", username).commit();
        editor.putString("password", password).commit();
    }

    public void clearLoginDetails() {
        editor.remove("isLogin").commit();
        editor.remove("ip").commit();
        editor.remove("jsonData").commit();
        if(!isRemember()) {
            editor.remove("username").commit();
            editor.remove("password").commit();
        }
    }

    public boolean isUplodaOnlyWifi() {
        // TODO Auto-generated method stub
        return mPreferences.getBoolean("isUplodaOnlyWifi", false);
    }

    public void setUplodaOnlyWifi(boolean b) {
        editor.putBoolean("isUplodaOnlyWifi", b).commit();
    }

    public boolean isAutoPaused() {
        // TODO Auto-generated method stub
        return mPreferences.getBoolean("isAutoPaused", false);
    }

    public void setAutoPaused(boolean b) {
        editor.putBoolean("isAutoPaused", b).commit();
    }

    public boolean isAutoBackup() {
        // TODO Auto-generated method stub
        return mPreferences.getBoolean("isAutoBackup", false);
    }

    public void setAutoBackup(boolean b) {
        editor.putBoolean("isAutoBackup", b).commit();
    }

    public int getSelectedLockInterval() {
        // TODO Auto-generated method stub
        return mPreferences.getInt("lock_interval", 0);
    }

    public void setLockInterval(int pos) {
        editor.putInt("lock_interval", pos).commit();
    }


    public boolean isRemember() {
        return mPreferences.getBoolean("isRemember", false);
    }

    public void setRemember(boolean b) {
        editor.putBoolean("isRemember", b).commit();
    }


}
