package com.terramaster;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.terramaster.dialog.CreateNewDialog;
import com.terramaster.dialog.MoreOptionsDialog;
import com.terramaster.dialog.OnOptionSelectListener;
import com.terramaster.dialog.ShareDialog;
import com.terramaster.discover.DeviceData;
import com.terramaster.discover.DeviceService;
import com.terramaster.fragment.DeviceFilesFragment;
import com.terramaster.ftp.FTPHelper;
import com.terramaster.model.FTPFileItem;
import com.terramaster.task.OnSuccessListener;
import com.terramaster.utils.AlertUtils;
import com.terramaster.utils.BundleParamsKey;
import com.terramaster.utils.LogM;
import com.terramaster.utils.SharedPrefUtils;

import java.util.ArrayList;

public class DeviceFilesActivity extends BaseFragmentActivity {

    private static final int REQUEST_SELECT_FILES_SHARE = 1102;
    public FTPHelper ftpHelper = FTPHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_files);
        initObjects();
        pushFragment(new DeviceFilesFragment(null), false, true);
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        enableBackButton();
        enableRefreshButton();
        setTitle(prefUtils.getDeviceData().HOSTNAME);

        findViewById(R.id.tvHome).setOnClickListener(this);
        findViewById(R.id.tvUpload).setOnClickListener(this);
        //Jaylin
        //findViewById(R.id.tvShare).setOnClickListener(this);
        findViewById(R.id.tvMore).setOnClickListener(this);

    }
    private boolean appInstalledOrNot(String pkg) {
        PackageManager localPackageManager = getPackageManager();
        try {
            localPackageManager.getPackageInfo(pkg, 1);
            return true;
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {
            return false;
        }

    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.tvBackButton:
                onBackPressed();
                break;
            case R.id.ivRefreshBtn:
                lastElement().reloadData();
                break;
            case R.id.tvHome:
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                break;
            case R.id.tvUpload:
                FTPFileItem directory = ((DeviceFilesFragment) lastElement()).getFTPFileItem();
                if (directory == null) {
                    AlertUtils.showSimpleAlert(DeviceFilesActivity.this, getString(R.string.e_upload_to_root_directory));
                } else {
                    Intent intentLocalFiles = new Intent(DeviceFilesActivity.this, LocalFilesActivity.class);
                    intentLocalFiles.putExtra(BundleParamsKey.OBJECT_KEY, directory);
                    startActivity(intentLocalFiles);
                }
                break;
//            case R.id.tvShare:
//                Intent intentSelectFiles = new Intent(DeviceFilesActivity.this, SelectDeviceFilesActivity.class);
//                intentSelectFiles.putExtra(BundleParamsKey.SELECT_FILES_ACTION_KEY, BundleParamsKey.SHARE_ACTION);
//                startActivityForResult(intentSelectFiles, REQUEST_SELECT_FILES_SHARE);
//
//                break;
            case R.id.tvMore:
                new MoreOptionsDialog(DeviceFilesActivity.this, new OnOptionSelectListener() {

                    @Override
                    public void onSelectOption(int id) {
                        // TODO Auto-generated method stub
                        switch (id) {
                            case R.id.llCreateNewFolder:
                                FTPFileItem directory = ((DeviceFilesFragment) lastElement()).getFTPFileItem();
                                if (directory == null) {
                                    AlertUtils.showSimpleAlert(DeviceFilesActivity.this, getString(R.string.e_upload_to_root_directory));
                                } else {
                                    new CreateNewDialog(DeviceFilesActivity.this, directory, new OnSuccessListener() {

                                        @Override
                                        public void onSuccess() {
                                            // TODO Auto-generated method stub
                                            lastElement().reloadData();
                                        }
                                    }).show();
                                }
                                break;
                            case R.id.llFileDownload:
                                Intent intentSelectFiles = new Intent(DeviceFilesActivity.this, SelectDeviceFilesActivity.class);
                                intentSelectFiles.putExtra(BundleParamsKey.SELECT_FILES_ACTION_KEY, BundleParamsKey.DOWNLOADFILE_ACTION);
                                startActivity(intentSelectFiles);
                                break;
                            default:
                                break;
                        }
                    }
                }).show();
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_SELECT_FILES_SHARE && resultCode == RESULT_OK) {
            ArrayList<String> list = data.getStringArrayListExtra(BundleParamsKey.DATA_KEY);
            DeviceData deviceData= prefUtils.getDeviceData();
            DeviceService deviceServiceSYC=deviceData==null?null:deviceData.getDeviceService("sys");
            ShareDialog.startShareLinks(DeviceFilesActivity.this,""+(deviceServiceSYC==null?"":deviceServiceSYC.port), list);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
