package com.terramaster;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.terramaster.dbhelper.DatabaseHelper;
import com.terramaster.utils.BundleParamsKey;
import com.terramaster.utils.LogM;
import com.terramaster.utils.SharedPrefUtils;

import me.zhanghai.patternlock.PatternLockUtils;


@SuppressWarnings("deprecation")
public class BaseActivity extends ActionBarActivity implements OnClickListener {
    public static boolean needToCheckLock = false;
    public SharedPrefUtils prefUtils;
    public DatabaseHelper dbHelper;
    private TextView tvTitle, tvBackButton, tvRightHeaderButton;
    private ImageView ivRefreshBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefUtils = SharedPrefUtils.getInstance(getApplicationContext());
        dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        try {
            loadDataFromBundle();
        } catch (Exception e) {
            LogM.w("loadDataFromBundle " + e.getMessage());
            e.printStackTrace();
        }

        // boolean isLaunchFromHistory = (getIntent().getFlags() &
        // Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
        // if (isLaunchFromHistory)
        // {
        // openPatternActivity();
        // }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (needToCheckLock) {
            needToCheckLock = false;
            openPatternActivity();
        }
    }

    protected void initObjects() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvBackButton = (TextView) findViewById(R.id.tvBackButton);

        ivRefreshBtn = (ImageView) findViewById(R.id.ivRefreshBtn);

        tvRightHeaderButton = (TextView) findViewById(R.id.tvRightHeaderButton);

    }

    protected void loadDataFromBundle() {

    }

    public void setTitle(String title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    @Override
    public void setTitle(int resId) {
        if (tvTitle != null) {
            tvTitle.setText(resId);
        }
    }

    public void enableBackButton() {
        if (tvBackButton != null) {
            tvBackButton.setOnClickListener(this);
            tvBackButton.setVisibility(View.VISIBLE);
        }
    }

    public void enableRefreshButton() {
        if (ivRefreshBtn != null) {
            ivRefreshBtn.setOnClickListener(this);
            ivRefreshBtn.setVisibility(View.VISIBLE);
        }
    }

    public void enableRightButton(String title) {
        if (tvRightHeaderButton != null) {
            tvRightHeaderButton.setText(title);
            tvRightHeaderButton.setOnClickListener(this);
            tvRightHeaderButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {

    }

    public void showToast(final String message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Toast.makeText(BaseActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // @Override
    // protected void onStart()
    // {
    // // TODO Auto-generated method stub
    // super.onStart();
    // if (isAskPattern)
    // {
    // isAskPattern = false;
    // openPatternActivity();
    //
    // }
    // }

    protected void openPatternActivity() {
        if (PatternLockUtils.hasPattern(getApplicationContext())) {
            Intent intentCL = new Intent(BaseActivity.this, ConfirmPatternActivity.class);
            intentCL.putExtra(BundleParamsKey.CONFIRM_PATTERN_TYPE_KEY, ConfirmPatternActivity.CHECK_PATTERN);
            startActivityForResult(intentCL, ConfirmPatternActivity.REQUEST_PATTERN);
        }
    }

    // @Override
    // protected void onStop()
    // {
    // // TODO Auto-generated method stub
    // isAskPattern = true;
    // super.onStop();
    // }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == ConfirmPatternActivity.REQUEST_PATTERN) {
            if (resultCode != RESULT_OK) {
                System.exit(0);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
