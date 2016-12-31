package com.terramaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.terramaster.utils.BundleParamsKey;

public class BackupModeActivity extends BaseActivity {
    private ImageView ivRightAllPhotos, ivRightNewPhotos;

    private boolean isBackupAllPhotos = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_mode);

        initObjects();
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        setTitle(R.string.backup_mode);
        enableBackButton();

        findViewById(R.id.btnFinish).setOnClickListener(this);
        ivRightNewPhotos = (ImageView) findViewById(R.id.ivRightNewPhotos);
        ivRightAllPhotos = (ImageView) findViewById(R.id.ivRightAllPhotos);

        updateSelection();
    }

    private void updateSelection() {
        ivRightAllPhotos.setVisibility(isBackupAllPhotos ? View.VISIBLE : View.INVISIBLE);
        ivRightNewPhotos.setVisibility(isBackupAllPhotos ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.tvBackButton:
                onBackPressed();
                break;
            case R.id.btnFinish:
                Intent intent = new Intent();
                intent.putExtra(BundleParamsKey.IS_BACKUP_ALL_PHOTOS_KEY, isBackupAllPhotos);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.rlOnlyNewPhotos:
                if (isBackupAllPhotos) {
                    isBackupAllPhotos = false;
                    updateSelection();
                }
                break;
            case R.id.rlAllPhotos:
                if (!isBackupAllPhotos) {
                    isBackupAllPhotos = true;
                    updateSelection();
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }
}
