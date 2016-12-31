package com.terramaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.terramaster.dbhelper.DBKeys;
import com.terramaster.dialog.SuccessToast;
import com.terramaster.discover.DeviceData;
import com.terramaster.fragment.SelectDeviceFilesFragment;
import com.terramaster.ftp.FTPHelper;
import com.terramaster.model.FTPFileItem;
import com.terramaster.model.TaskDetail;
import com.terramaster.service.BackgroundTaskService;
import com.terramaster.utils.AlertUtils;
import com.terramaster.utils.BundleParamsKey;
import com.terramaster.utils.SDCardUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class SelectDeviceFilesActivity extends BaseFragmentActivity {
    public FTPHelper ftpHelper = FTPHelper.getInstance();
    private HashMap<String, FTPFileItem> hashMapSelected = new HashMap<>();
    private int action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_device_files);
        initObjects();
        pushFragment(new SelectDeviceFilesFragment(null), false, true);
    }

    @Override
    protected void loadDataFromBundle() {
        // TODO Auto-generated method stub
        super.loadDataFromBundle();
        action = getIntent().getIntExtra(BundleParamsKey.SELECT_FILES_ACTION_KEY, 0);
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        enableBackButton();
        enableRightButton(getString(R.string.confirm));
        updateTitle();
    }

    public void updateTitle() {
        if (hashMapSelected.size() <= 0) {
            setTitle(getString(R.string.select_file));
        } else {
            setTitle(hashMapSelected.size() + " " + getString(R.string.file_selected));
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.tvBackButton:
                onBackPressed();
                break;
            case R.id.tvRightHeaderButton:
                doProperAction();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void doProperAction() {
        // TODO Auto-generated method stub
        Set<String> keySets = hashMapSelected.keySet();
        if (keySets.size() <= 0) {
            AlertUtils.showSimpleAlert(SelectDeviceFilesActivity.this, getString(action == BundleParamsKey.DOWNLOADFILE_ACTION ? R.string.e_select_file_download : R.string.e_select_file_share));
            return;
        }
        if (action == BundleParamsKey.DOWNLOADFILE_ACTION) {
            DeviceData deviceData = prefUtils.getDeviceData();

            for (String key : keySets) {
                FTPFileItem item = hashMapSelected.get(key);
                TaskDetail taskDetail = new TaskDetail();
                taskDetail.setTaskFileSize(item.getSize());
                taskDetail.setFrom(item.getPath());
                Date date = new Date();
                taskDetail.setLastUpdated(date);
                taskDetail.setCreatedAt(date);
                taskDetail.setTaskType(DBKeys.TASK_DOWNLOADING);
                taskDetail.setTaskStatus(DBKeys.STATUS_WAITING);
                taskDetail.setMacAddress(deviceData.getMac());
                long id = dbHelper.insertTask(taskDetail);
                taskDetail.setRowId(id);

                Intent intent = new Intent(Intent.ACTION_SYNC, null, SelectDeviceFilesActivity.this, BackgroundTaskService.class);
                intent.putExtra(BundleParamsKey.OBJECT_KEY, taskDetail);
                intent.putExtra(BundleParamsKey.SERVICE_ACTION_KEY, BundleParamsKey.START_ACTION);
                startService(intent);
            }
            new SuccessToast(SelectDeviceFilesActivity.this,
                    getString(R.string.success_download),
                    getString(R.string.download_tip)+"/"+SDCardUtils.getDownloadingPath().getName()).show();
        } else if (action == BundleParamsKey.SHARE_ACTION) {
            ArrayList<String> list = new ArrayList<>();
            for (String key : keySets) {
                FTPFileItem item = hashMapSelected.get(key);
                list.add(item.getPath());
            }
            Intent data = new Intent();
            data.putExtra(BundleParamsKey.DATA_KEY, list);
            setResult(RESULT_OK, data);
        }

        finish();
    }

    public boolean isSelected(FTPFileItem fileItem) {
        String key = fileItem.getPath() + fileItem.getFileSize();
        return hashMapSelected.containsKey(key);
    }

    public void toggleSelection(FTPFileItem fileItem) {
        String key = fileItem.getPath() + fileItem.getFileSize();
        if (hashMapSelected.containsKey(key)) {
            hashMapSelected.remove(key);
        } else {
            hashMapSelected.put(key, fileItem);
        }
        updateTitle();
    }

}
