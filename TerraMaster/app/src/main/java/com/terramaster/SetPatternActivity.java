/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.terramaster;

import android.os.Bundle;
import android.view.View;

import java.util.List;

import me.zhanghai.patternlock.PatternLockUtils;
import me.zhanghai.patternlock.PatternView;

public class SetPatternActivity extends me.zhanghai.patternlock.BaseSetPatternActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//	setContentView(R.layout.activity_password_lock);
        initObjects();
        // AppUtils.setupActionBarDisplayUp(this);
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        enableBackButton();
        setTitle(PatternLockUtils.hasPattern(getApplicationContext()) ? R.string.modify_pattern_lock : R.string.set_pattern_lock);

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
    protected void onSetPattern(List<PatternView.Cell> pattern) {
        PatternLockUtils.setPattern(pattern, this);
    }
}
