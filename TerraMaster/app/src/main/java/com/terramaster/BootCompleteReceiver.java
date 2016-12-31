package com.terramaster;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.terramaster.service.AutoBackupService;
import com.terramaster.utils.SharedPrefUtils;

import java.util.Calendar;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        SharedPrefUtils prefUtils = SharedPrefUtils.getInstance(context);
        if (prefUtils.isLogin() && prefUtils.isRemember() && prefUtils.isAutoBackup()) {
            Intent intentAlarm = new Intent(context, AutoBackupService.class);
            PendingIntent pintent = PendingIntent.getService(context, 0, intentAlarm, 0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 30 * 60 * 1000, pintent);

        }
    }

}
