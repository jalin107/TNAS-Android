package com.terramaster.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.terramaster.dbhelper.DBKeys;
import com.terramaster.dbhelper.DatabaseHelper;
import com.terramaster.discover.DeviceData;
import com.terramaster.ftp.FTPHelper;
import com.terramaster.model.AlbumBackupDetail;
import com.terramaster.model.ImageBackupDetail;
import com.terramaster.task.WorkerThread;
import com.terramaster.utils.BundleParamsKey;
import com.terramaster.utils.IconUtils;
import com.terramaster.utils.SharedPrefUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AutoBackupService extends Service {
    public static final String UPDATE_RECEIVER = "com.terramaster.ACTION_AUTO_BACKUP";
    public static final String FTP_BACKUP_DIRECTORY = "public/Mobile backup";
    private static final long AUTO_BACKUP_INTERVAL = 30 * 60 * 1000;

    private FTPHelper ftpHelper;
    private DatabaseHelper dbHelper;

    private ExecuteTask thread;
    private ArrayList<AlbumBackupDetail> albumDetailList;
    private DeviceData deviceData;
    private SharedPrefUtils prefUtils;
    private boolean isBackupRunning;

    private int badTries;

    public AutoBackupService() {
        // TODO Auto-generated constructor stub
        showLog("Service inited");

    }

    private void showLog(String msg) {
        Log.i("auto_backup", msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        showLog("Service onStartCommand");
        if(!isBackupRunning){
            dbHelper = DatabaseHelper.getInstance(AutoBackupService.this);
            prefUtils = SharedPrefUtils.getInstance(AutoBackupService.this);
            deviceData = prefUtils.getDeviceData();
            ftpHelper = new FTPHelper();
            isBackupRunning = false;
            badTries = 0;
            doFTPLogin();
        }
        else{
            showLog("Service onStartCommand else");
        }


        return START_REDELIVER_INTENT;
    }

    private void doFTPLogin() {
        if (ftpHelper.isLogin()) {
            if (!isBackupRunning) {
                createBackupFolderIfNotExist();
            }
            return;
        }
        if (badTries == 10) {
            badTries = 0;
            return;
        }
        new WorkerThread(AutoBackupService.this) {

            @Override
            public String onWorkInBackground() {
                // TODO Auto-generated method stub
                try {
                    String userName = prefUtils.getUsername();
                    String password = prefUtils.getPassword();
                    ftpHelper.logIn(deviceData.IPV4, deviceData.getDeviceService("ftp").port, userName, password);

                } catch (Exception e) {

                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected synchronized void onWorkFinished(String result) {
                // TODO Auto-generated method stub
                super.onWorkFinished(result);
                if (ftpHelper.isLogin()) {
                    showLog("Login success...");
                    createBackupFolderIfNotExist();
                } else {
                    badTries++;
                    doFTPLogin();
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        showLog("AutoBackup destroyed");
        isBackupRunning = false;
        super.onDestroy();
    }

    private void createBackupFolderIfNotExist() {
        if (isBackupRunning) {
            return;
        }
        isBackupRunning = true;
        badTries = 0;
        albumDetailList = dbHelper.getAllAlbumBackupList(deviceData.getMac());
        new WorkerThread(AutoBackupService.this) {

            @Override
            public String onWorkInBackground() {
                // TODO Auto-generated method stub
                try {
                    try {
                        ftpHelper.createNewFolder(FTP_BACKUP_DIRECTORY);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected synchronized void onWorkFinished(String result) {
                // TODO Auto-generated method stub
                super.onWorkFinished(result);
                findAndStartImageBackup();
            }
        }.execute();
    }

    private void endAutoBackTask() {
        isBackupRunning = false;
        if (prefUtils.isAutoBackup()) {
            Intent intent = new Intent(AutoBackupService.this, AutoBackupService.class);
            PendingIntent pintent = PendingIntent.getService(AutoBackupService.this, 505, intent, 0);
            AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(pintent);

            alarm.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + AUTO_BACKUP_INTERVAL, pintent);
        }
    }

    private void findAndStartImageBackup() {
        showLog("findAndStartImageBackup");
        if (albumDetailList.size() <= 0 || !prefUtils.isAutoBackup()) {
            showLog("No more folder founds");
            endAutoBackTask();
            return;
        }
        AlbumBackupDetail albumBackupDetail = albumDetailList.get(0);
        if (!albumBackupDetail.isEnabled()) {
            showLog("Album not enabled " + albumBackupDetail.getRowId());
            albumDetailList.remove(0);
            findAndStartImageBackup();
            return;
        }
        boolean isTaskStart = false;
        File[] allImages = new File(albumBackupDetail.getFrom()).listFiles();
        for (File file : allImages) {
            if (!IconUtils.isPhoto(file.getName())) {
                continue;
            }
            ImageBackupDetail detail = dbHelper.getImageBackup(albumBackupDetail.getRowId(), file.getAbsolutePath(), deviceData.getMac());
            if (detail != null) {
                showLog("Status: " + detail.getTaskStatus());
                if (detail.getTaskStatus() != DBKeys.STATUS_ERROR && detail.getTaskStatus() != DBKeys.STATUS_SKIPED && detail.getTaskStatus() != DBKeys.STATUS_COMPLETE) {
                    isTaskStart = true;
                    detail.setTaskStatus(DBKeys.STATUS_WAITING);
                    updateImageBackupDetail(detail);
                    startTask(detail);
                    break;
                }
            } else {
                detail = new ImageBackupDetail();
                detail.setFrom(file.getAbsolutePath());
                detail.setAlbumId(albumBackupDetail.getRowId());
                Date date = new Date();
                detail.setLastUpdated(date);
                detail.setCreatedAt(date);
                detail.setTaskStatus(DBKeys.STATUS_WAITING);
                detail.setMacAddress(deviceData.getMac());

                long id = dbHelper.insertImageBackup(detail);
                detail.setRowId(id);
                startTask(detail);

                isTaskStart = true;
                break;
            }
        }
        if (!isTaskStart) {
            showLog("Album backup completed " + albumBackupDetail.getRowId());
            albumDetailList.remove(0);
            findAndStartImageBackup();
        }
    }

    private void startTask(final ImageBackupDetail detail) {
        if (detail.getTaskStatus() == DBKeys.STATUS_WAITING) {
            showLog("Image upload start " + detail.getRowId() + " : " + detail.getFrom());
            thread = new ExecuteTask(AutoBackupService.this, detail);
            thread.execute();
        } else {
            findAndStartImageBackup();
        }

    }

    private void updateImageBackupDetail(ImageBackupDetail detail) {
        detail.setLastUpdated(new Date());
        dbHelper.updateImageBackupDetail(detail);

        if (detail.getTaskStatus() == DBKeys.STATUS_COMPLETE) {
            Intent intentBroadcats = new Intent();
            intentBroadcats.setAction(UPDATE_RECEIVER);
            intentBroadcats.putExtra(BundleParamsKey.OBJECT_KEY, detail);
            sendBroadcast(intentBroadcats);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private void doTask(ImageBackupDetail detail) throws Exception {

        File file = new File(detail.getFrom());

        boolean success = ftpHelper.uploadFileToFolder(file, FTP_BACKUP_DIRECTORY);
        detail.setTo(FTP_BACKUP_DIRECTORY + "/" + file.getName());
        showLog("File upload: " + detail.getTo() + " : " + success);
        if (success) {
            detail.setTaskStatus(DBKeys.STATUS_COMPLETE);
        } else {
            detail.setTaskStatus(DBKeys.STATUS_ERROR);
        }

    }

    private class ExecuteTask extends WorkerThread {
        public ImageBackupDetail detail;

        public ExecuteTask(Context context, ImageBackupDetail detail) {
            super(context);
            // TODO Auto-generated constructor stub
            this.detail = detail;
        }

        @Override
        protected void onWorkStarted() {
            // TODO Auto-generated method stub
            super.onWorkStarted();
            detail.setTaskStatus(DBKeys.STATUS_RUNNING);
            updateImageBackupDetail(detail);

        }

        @Override
        public String onWorkInBackground() {
            // TODO Auto-generated method stub
            try {
                doTask(detail);
            } catch (Exception e) {
                e.printStackTrace();
                detail.setTaskStatus(DBKeys.STATUS_ERROR);
            }
            return null;
        }

        @Override
        protected synchronized void onWorkCancelled(String result) {
            // TODO Auto-generated method stub
            super.onWorkCancelled(result);
            new WorkerThread(mContext) {

                @Override
                public String onWorkInBackground() {
                    // TODO Auto-generated method stub
                    try {
                        ftpHelper.deleteFile(detail.getTo());
                    } catch (Exception e) {

                    }
                    return null;
                }
            }.execute();

        }

        @Override
        protected synchronized void onWorkFinished(String result) {
            // TODO Auto-generated method stub
            super.onWorkFinished(result);
            updateImageBackupDetail(detail);
            thread = null;
            findAndStartImageBackup();
        }
    }

}
