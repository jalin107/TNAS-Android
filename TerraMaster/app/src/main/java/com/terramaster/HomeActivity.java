package com.terramaster;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.terramaster.discover.DeviceData;
import com.terramaster.service.AutoBackupService;
import com.terramaster.widget.MyTextView;

public class HomeActivity extends BaseActivity {
    private static Activity activity;

    public static void finishActivity() {
        if (activity != null) {
            activity.finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        needToCheckLock = true;
        if (prefUtils.isAutoBackup()) {
            Intent intent = new Intent(Intent.ACTION_SYNC, null, HomeActivity.this, AutoBackupService.class);
            startService(intent);
        }
        setContentView(R.layout.activity_home);
        activity = this;
        initObjects();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        activity = null;
        super.onDestroy();
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        setTitle(R.string.home);

        final DeviceData deviceData = prefUtils.getDeviceData();
        ((MyTextView) findViewById(R.id.tvUsername)).setText(prefUtils.getUsername());
        ((MyTextView) findViewById(R.id.tvDeviceName)).setText(deviceData.HOSTNAME);
        ((MyTextView) findViewById(R.id.tvDeviceAddress)).setText("IP: " + deviceData.IPV4 + ":" + deviceData.getDeviceService("ftp").port);
        ((MyTextView) findViewById(R.id.tvStorageStatus)).setText("MAC: " + deviceData.MAC);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnAllFiles:
                startActivity(new Intent(HomeActivity.this, DeviceFilesActivity.class));
                break;
            case R.id.btnAlbumBackup:
                startActivity(new Intent(HomeActivity.this, AutoBackupActivity.class));
                break;
            case R.id.btnTasksList:
                startActivity(new Intent(HomeActivity.this, TaskListActivity.class));
                break;
            case R.id.ivProfilePicture:
            case R.id.btnSettings:
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                break;
            default:
                super.onClick(v);
                break;
        }

    }
}
