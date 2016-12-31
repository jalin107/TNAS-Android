/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.terramaster;

import android.os.Bundle;
import android.view.View;

import com.terramaster.utils.BundleParamsKey;

import java.util.List;

import me.zhanghai.patternlock.PatternLockUtils;
import me.zhanghai.patternlock.PatternPrefUtils;
import me.zhanghai.patternlock.PatternView;

public class ConfirmPatternActivity extends me.zhanghai.patternlock.BaseConfirmPatternActivity {
    public static final int REQUEST_PATTERN = 2986;

    public static final int CHECK_PATTERN = 1;
    public static final int CLOSE_PATTERN_LOCK = 2;
    public static final int MODIFY_PATTERN = 3;

    private int checkType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initObjects();

    }

    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        enableBackButton();
        if (checkType == CLOSE_PATTERN_LOCK) {
            setTitle(R.string.close_pattern_lock);
        } else if (checkType == MODIFY_PATTERN) {
            setTitle(R.string.modify_pattern_lock);
        } else {
            setTitle(R.string.check_pattern);
        }

    }

    @Override
    protected void loadDataFromBundle() {
        // TODO Auto-generated method stub
        super.loadDataFromBundle();
        checkType = getIntent().getIntExtra(BundleParamsKey.CONFIRM_PATTERN_TYPE_KEY, CHECK_PATTERN);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.tvBackButton:
                onBackPressed();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected boolean isStealthModeEnabled() {
        return !PatternPrefUtils.getBoolean(PatternLockUtils.KEY_PATTERN_VISIBLE, PatternLockUtils.DEFAULT_PATTERN_VISIBLE, this);
    }

    @Override
    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        return PatternLockUtils.isPatternCorrect(pattern, this);
    }

    @Override
    protected void onForgotPassword() {

        // Finish with RESULT_FORGOT_PASSWORD.
        super.onForgotPassword();
    }
}
