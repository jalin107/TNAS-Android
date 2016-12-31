package com.terramaster.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.terramaster.R;

public class InternetUtils {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public static void showInternetAlert(final Context context, final boolean isForseClose) {
        AlertDialog dialog = new AlertDialog.Builder(context).setIcon(0).setTitle(R.string.no_connection).setMessage(R.string.e_no_internet).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isForseClose) {
                    ((Activity) context).finish();
                }
            }
        }).show();
        AlertUtils.changeDefaultColor(dialog);

    }
}
