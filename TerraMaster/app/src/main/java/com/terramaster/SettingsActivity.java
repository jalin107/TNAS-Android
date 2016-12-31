package com.terramaster;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.terramaster.utils.AlertUtils;
import com.terramaster.utils.SDCardUtils;
import com.terramaster.widget.MyTextView;

import me.zhanghai.patternlock.PatternLockUtils;

public class SettingsActivity extends BaseActivity {

    private ImageView ivUploadOnlyWifi;
    private MyTextView tvPasswordLock, tvLocalFileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initObjects();
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        enableBackButton();
        setTitle(R.string.settings);
        ivUploadOnlyWifi = (ImageView) findViewById(R.id.ivUploadOnlyWifi);
        ivUploadOnlyWifi.setSelected(prefUtils.isUplodaOnlyWifi());
        tvPasswordLock = (MyTextView) findViewById(R.id.tvPasswordLock);
        tvLocalFileSize = (MyTextView) findViewById(R.id.tvLocalFileSize);
        tvLocalFileSize.setText(SDCardUtils.getCacheFolderSize());
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            ((MyTextView) findViewById(R.id.tvVersion)).setText(pInfo.versionName);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        tvPasswordLock.setText(PatternLockUtils.hasPattern(getApplicationContext()) ? R.string.tenable : R.string.untenable);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.rlPasswordLock:
                startActivity(new Intent(SettingsActivity.this, PatternLockActivity.class));
                break;

            case R.id.rlUploadOverWifi:
                ivUploadOnlyWifi.setSelected(!ivUploadOnlyWifi.isSelected());
                prefUtils.setUplodaOnlyWifi(ivUploadOnlyWifi.isSelected());
                break;
            case R.id.rlClearLocal:
                if (tvLocalFileSize.getText().toString().equals("0 B")) {
                    return;
                }
                AlertUtils.showConfirmAlert(SettingsActivity.this, getString(R.string.confirm), getString(R.string.confirm_clear_local_files), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        SDCardUtils.clearCacheFolder();
                        tvLocalFileSize.setText(SDCardUtils.getCacheFolderSize());
                    }
                });
                break;
//            case R.id.rlInstructions:
                // startActivity(new Intent(HomeActivity.this,
                // LocalFilesActivity.class));
//                break;
            case R.id.rlFeedback:
                try {
                    Intent intentFeedback = new Intent(Intent.ACTION_VIEW);
                    Uri data = Uri.parse("mailto:support@terra-master.com?subject=Feedback from Android App&body=");
                    intentFeedback.setData(data);
                    startActivity(intentFeedback);
                } catch (Exception e) {
                    // TODO: handle exception
                    showToast(getString(R.string.e_no_email_app_found));
                }
                break;
            case R.id.btnLogout:
                PatternLockUtils.clearPattern(getApplicationContext());
                dbHelper.clearAll();
                prefUtils.setLockInterval(0);
                prefUtils.setUplodaOnlyWifi(true);
                prefUtils.clearLoginDetails();
                HomeActivity.finishActivity();
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;
            case R.id.tvBackButton:
                onBackPressed();
                break;
            default:
                super.onClick(v);
                break;
        }

    }
}
