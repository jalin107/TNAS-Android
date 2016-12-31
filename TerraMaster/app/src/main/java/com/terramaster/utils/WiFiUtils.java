package com.terramaster.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.terramaster.R;

public class WiFiUtils {
    public static final int REQUEST_WIFI_SETTING = 1209;

    public static boolean isWiFiEnbled(Context context) {
//	WifiManager mng = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//	
//	return mng.isWifiEnabled();
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }

    public static void openWiFiSetting(final Activity activity) {
        AlertUtils.showConfirmAlert(activity, activity.getString(R.string.wifi), activity.getString(R.string.confirm_wifi_setting), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                activity.startActivityForResult(gpsOptionsIntent, REQUEST_WIFI_SETTING);
            }
        });

    }

}
