package com.terramaster;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.terramaster.discover.DeviceData;
import com.terramaster.ftp.FTPHelper;
import com.terramaster.task.WorkerThread;

public class SplashActivity extends BaseActivity {
    private final long SPLASH_MIN_TIME = 2000;
    private Handler mHandler = new Handler();
    private FTPHelper ftpHelper = FTPHelper.getInstance();
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (prefUtils.isFirstTime()) {
                Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                if (prefUtils.isLogin() && prefUtils.isRemember() && ftpHelper != null && ftpHelper.isLogin()) {
                    Intent intent = new Intent(SplashActivity.this, DeviceFilesActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        needToCheckLock = false;
        setContentView(R.layout.activity_splash);

        startSplash();

    }

    private void startSplash() {
        if (prefUtils.isFirstTime() || !prefUtils.isRemember() || !prefUtils.isLogin() || FTPHelper.getInstance().isLogin()) {
            mHandler.postDelayed(mRunnable, SPLASH_MIN_TIME);
        } else {
            final DeviceData deviceData = prefUtils.getDeviceData();
            if (deviceData == null) {
                mHandler.postDelayed(mRunnable, SPLASH_MIN_TIME);
                return;
            }
            final long startTime = System.currentTimeMillis();
            new WorkerThread(SplashActivity.this) {


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
                    long diff = System.currentTimeMillis() - startTime;
                    if (diff < SPLASH_MIN_TIME) {
                        mHandler.postDelayed(mRunnable, SPLASH_MIN_TIME - diff);
                    } else {
                        mHandler.post(mRunnable);
                    }
                }
            }.start();
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        super.onBackPressed();
    }
}
