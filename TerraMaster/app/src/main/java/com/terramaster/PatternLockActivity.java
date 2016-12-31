package com.terramaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.terramaster.utils.BundleParamsKey;
import com.terramaster.widget.MyTextView;

import me.zhanghai.patternlock.PatternLockUtils;

public class PatternLockActivity extends BaseActivity {
    private final int REQUEST_SET_PATTERN = 2110;
    private final int REQUEST_CLOSE_PATTERN = 2111;
    private final int REQUEST_MODIFY_PATTERN = 2112;
    private ImageView ivPasswordLockSwitch;
    private View llOthersSetting;
    private MyTextView tvLockInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_lock);

        initObjects();
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        enableBackButton();
        setTitle(R.string.pattern_lock_settings);
        llOthersSetting = findViewById(R.id.llOthersSetting);
        tvLockInterval = (MyTextView) findViewById(R.id.tvLockInterval);

        ivPasswordLockSwitch = (ImageView) findViewById(R.id.ivPasswordLockSwitch);
        setPasswordLockSwitch(PatternLockUtils.hasPattern(getApplicationContext()));

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (llOthersSetting.getVisibility() == View.VISIBLE) {
            tvLockInterval.setText(getResources().getStringArray(R.array.lock_intervals)[prefUtils.getSelectedLockInterval()]);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.rlPasswordLockSwitch:
                if (PatternLockUtils.hasPattern(getApplicationContext())) {
                    Intent intentCL = new Intent(PatternLockActivity.this, ConfirmPatternActivity.class);
                    intentCL.putExtra(BundleParamsKey.CONFIRM_PATTERN_TYPE_KEY, ConfirmPatternActivity.CLOSE_PATTERN_LOCK);
                    startActivityForResult(intentCL, REQUEST_CLOSE_PATTERN);
                } else {
                    startActivityForResult(new Intent(PatternLockActivity.this, SetPatternActivity.class), REQUEST_SET_PATTERN);
                }
                break;

            case R.id.rlLockInterval:
                startActivity(new Intent(PatternLockActivity.this, LockIntervalActivity.class));
                break;
            case R.id.rlModifyPasswordLock:
                Intent intentM = new Intent(PatternLockActivity.this, ConfirmPatternActivity.class);
                intentM.putExtra(BundleParamsKey.CONFIRM_PATTERN_TYPE_KEY, ConfirmPatternActivity.MODIFY_PATTERN);
                startActivityForResult(intentM, REQUEST_MODIFY_PATTERN);
                break;

            case R.id.tvBackButton:
                onBackPressed();
                break;
            default:
                super.onClick(v);
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SET_PATTERN) {
                setPasswordLockSwitch(PatternLockUtils.hasPattern(getApplicationContext()));
            } else if (requestCode == REQUEST_CLOSE_PATTERN) {
                PatternLockUtils.clearPattern(getApplicationContext());
                setPasswordLockSwitch(PatternLockUtils.hasPattern(getApplicationContext()));
            } else if (requestCode == REQUEST_MODIFY_PATTERN) {
                startActivityForResult(new Intent(PatternLockActivity.this, SetPatternActivity.class), REQUEST_SET_PATTERN);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setPasswordLockSwitch(boolean isLock) {
        llOthersSetting.setVisibility(isLock ? View.VISIBLE : View.GONE);
        ivPasswordLockSwitch.setSelected(isLock);
    }
}
