package com.terramaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.terramaster.dbhelper.DBKeys;
import com.terramaster.discover.DeviceData;
import com.terramaster.model.AlbumBackupDetail;
import com.terramaster.model.ImageBackupDetail;
import com.terramaster.service.AutoBackupService;
import com.terramaster.utils.IconUtils;
import com.terramaster.widget.MyTextView;

import java.io.File;
import java.util.List;

public class AutoBackupActivity extends BaseActivity {
    private final int REQUEST_ENABLE_AUTOBACKUP = 1020;

    private ImageView ivAlbumAutoBackup, ivAutoPause;
    private MyTextView tvNoOfPhotos, tvBackupPhotosTo;

    private DeviceData deviceData;

    private boolean isAutoBackupEnabled;
    private BroadcastReceiver mAutoBackUpadateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            updateAutoBackupStatus();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_backup);

        initObjects();

        IntentFilter filter = new IntentFilter();
        filter.addAction(AutoBackupService.UPDATE_RECEIVER);
        registerReceiver(mAutoBackUpadateReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (mAutoBackUpadateReceiver != null) {
            unregisterReceiver(mAutoBackUpadateReceiver);
            mAutoBackUpadateReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        setTitle(R.string.auto_backup);
        enableBackButton();

        findViewById(R.id.btnEnableAutoBackup).setOnClickListener(this);

        deviceData = prefUtils.getDeviceData();
        boolean isAutoBackupAvailable = dbHelper.isAutoBackupAvailable(deviceData.getMac());
        View llAutoBackup = findViewById(R.id.llAutoBackup);
        View llEnableAutoBackup = findViewById(R.id.llEnableAutoBackup);
        llEnableAutoBackup.setVisibility(isAutoBackupAvailable ? View.GONE : View.VISIBLE);
        llAutoBackup.setVisibility(isAutoBackupAvailable ? View.VISIBLE : View.GONE);

        if (isAutoBackupAvailable) {
            ivAlbumAutoBackup = (ImageView) findViewById(R.id.ivAlbumAutoBackup);

            ivAutoPause = (ImageView) findViewById(R.id.ivAutoPause);
            ivAutoPause.setSelected(prefUtils.isAutoPaused());

            tvNoOfPhotos = (MyTextView) findViewById(R.id.tvNoOfPhotos);
            tvBackupPhotosTo = (MyTextView) findViewById(R.id.tvBackupPhotosTo);
            tvBackupPhotosTo.setText(getString(R.string.backup_photos_to) + " " + AutoBackupService.FTP_BACKUP_DIRECTORY);

            updateAutoBackupStatus();
            updateAutoBackupEnable();
        }
    }

    private void updateAutoBackupStatus() {
        int count=dbHelper.getBackedupPhotosCount();
        int countTotal=countTotalPhotosForBackup();
        if(countTotal<count){
            countTotal=count;
        }
        tvNoOfPhotos.setText(count + "/"+countTotal);
    }

    private int countTotalPhotosForBackup(){
        int count=0;
        List<AlbumBackupDetail> albumDetailList = dbHelper.getAllAlbumBackupList(deviceData.getMac());
        for (AlbumBackupDetail albumBackupDetail :
                albumDetailList) {
            if (!albumBackupDetail.isEnabled()) {
                continue;
            }
            File[] allImages = new File(albumBackupDetail.getFrom()).listFiles();
            for (File file : allImages) {
                if (!IconUtils.isPhoto(file.getName())) {
                    continue;
                }
                ImageBackupDetail detail = dbHelper.getImageBackup(albumBackupDetail.getRowId(), file.getAbsolutePath(), deviceData.getMac());
                if (detail != null && detail.getTaskStatus() == DBKeys.STATUS_SKIPED) {
                    continue;
                }
                count++;
            }
        }
        return count;
    }

    private void updateAutoBackupEnable() {
        // TODO Auto-generated method stub
        isAutoBackupEnabled = prefUtils.isAutoBackup();
        ivAlbumAutoBackup.setSelected(isAutoBackupEnabled);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.tvBackButton:
                onBackPressed();
                break;
            case R.id.btnEnableAutoBackup:
                startActivityForResult(new Intent(AutoBackupActivity.this, SelectPhotoAlbumActivity.class), REQUEST_ENABLE_AUTOBACKUP);
                break;
            case R.id.rlAutoPause:
                ivAutoPause.setSelected(!ivAutoPause.isSelected());
                prefUtils.setAutoPaused(ivAutoPause.isSelected());
                break;
            case R.id.rlAlbumAutoBackup:
                prefUtils.setAutoBackup(!isAutoBackupEnabled);
                updateAutoBackupEnable();
                if (isAutoBackupEnabled) {
                    Intent intent = new Intent(Intent.ACTION_SYNC, null, AutoBackupActivity.this, AutoBackupService.class);
                    startService(intent);
                }
                break;
            case R.id.rlSelectAlbum:
                startActivityForResult(new Intent(AutoBackupActivity.this, SelectPhotoAlbumActivity.class), REQUEST_ENABLE_AUTOBACKUP);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_ENABLE_AUTOBACKUP && resultCode == RESULT_OK) {
            initObjects();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
