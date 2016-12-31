package com.terramaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.LinearLayout;

import com.terramaster.dbhelper.DBKeys;
import com.terramaster.dialog.SuccessToast;
import com.terramaster.discover.DeviceData;
import com.terramaster.fragment.LocalFilesFragment;
import com.terramaster.model.FTPFileItem;
import com.terramaster.model.FileItem;
import com.terramaster.model.TaskDetail;
import com.terramaster.service.BackgroundTaskService;
import com.terramaster.utils.BundleParamsKey;
import com.terramaster.widget.MyButton;
import com.terramaster.widget.MyTextView;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class LocalFilesActivity extends BaseFragmentActivity {

    private HashMap<String, FileItem> hashMapSelected = new HashMap<>();

    private LinearLayout llUploadBar;
    private MyTextView tvUploadPath;
    private MyButton btnStartUpload;

    private FTPFileItem currentDIrectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_files);
        initObjects();
        pushFragment(new LocalFilesFragment(null), false, true);
    }

    @Override
    protected void loadDataFromBundle() {
        // TODO Auto-generated method stub
        super.loadDataFromBundle();
        currentDIrectory = (FTPFileItem) getIntent().getSerializableExtra(BundleParamsKey.OBJECT_KEY);
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        enableBackButton();
        llUploadBar = (LinearLayout) findViewById(R.id.llUploadBar);
        tvUploadPath = (MyTextView) findViewById(R.id.tvUploadPath);
        btnStartUpload = (MyButton) findViewById(R.id.btnStartUpload);
        btnStartUpload.setOnClickListener(this);

        String uploadTo = getString(R.string.upload_to);
        SpannableString spannableString = new SpannableString(uploadTo + " " + prefUtils.getDeviceData().HOSTNAME + "/" + (currentDIrectory == null ? "" : currentDIrectory.getPath()) + "/");
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, uploadTo.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        tvUploadPath.setText(spannableString);
        updateTitle();
    }

    public void updateTitle() {
        if (hashMapSelected.size() <= 0) {
            setTitle(getString(R.string.select_file));
            llUploadBar.setVisibility(View.GONE);
        } else {
            setTitle(hashMapSelected.size() + " " + getString(R.string.file_selected));
            llUploadBar.setVisibility(View.VISIBLE);
            btnStartUpload.setText(getString(R.string.start_upload) + " (" + hashMapSelected.size() + ")");
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        System.out.print("call onClick"+Thread.currentThread().getStackTrace()[2].getLineNumber());
        switch (v.getId()) {
            case R.id.tvBackButton:
                onBackPressed();
                break;

            case R.id.btnStartUpload:
                startUploadAllSelectedFiles();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void startUploadAllSelectedFiles() {
        // TODO Auto-generated method stub
        DeviceData deviceData = prefUtils.getDeviceData();

        Set<String> keySets = hashMapSelected.keySet();
        for (String key : keySets) {
            FileItem item = hashMapSelected.get(key);

            TaskDetail taskDetail = new TaskDetail();
            taskDetail.setFrom(item.getPath());
            taskDetail.setTo(currentDIrectory == null ? "" : currentDIrectory.getPath());
            Date date = new Date();
            taskDetail.setLastUpdated(date);
            taskDetail.setCreatedAt(date);
            taskDetail.setTaskType(DBKeys.TASK_UPLOADING);
            taskDetail.setTaskStatus(DBKeys.STATUS_WAITING);
            taskDetail.setMacAddress(deviceData.getMac());

            long id = dbHelper.insertTask(taskDetail);
            taskDetail.setRowId(id);

            Intent intent = new Intent(Intent.ACTION_SYNC, null, LocalFilesActivity.this, BackgroundTaskService.class);
            intent.putExtra(BundleParamsKey.OBJECT_KEY, taskDetail);
            intent.putExtra(BundleParamsKey.SERVICE_ACTION_KEY, BundleParamsKey.START_ACTION);
            startService(intent);
        }
        new SuccessToast(LocalFilesActivity.this, hashMapSelected.size() + " " + getString(R.string.success_upload)).show();
        hashMapSelected.clear();
        finish();
    }

    public boolean isSelected(FileItem fileItem) {
        String key = fileItem.getName() + fileItem.getFileSize();
        return hashMapSelected.containsKey(key);
    }

    public void toggleSelection(FileItem fileItem) {
        String key = fileItem.getName() + fileItem.getFileSize();
        if (hashMapSelected.containsKey(key)) {
            hashMapSelected.remove(key);
        } else {
            hashMapSelected.put(key, fileItem);
        }
        updateTitle();
    }
}