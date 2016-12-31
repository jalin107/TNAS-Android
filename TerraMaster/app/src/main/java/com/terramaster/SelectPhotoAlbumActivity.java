package com.terramaster;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.terramaster.adapter.SelectPhotoAlbumAdapter;
import com.terramaster.dbhelper.DBKeys;
import com.terramaster.discover.DeviceData;
import com.terramaster.model.AlbumBackupDetail;
import com.terramaster.model.AlbumDetail;
import com.terramaster.model.ImageBackupDetail;
import com.terramaster.service.AutoBackupService;
import com.terramaster.task.WorkerThread;
import com.terramaster.utils.AlertUtils;
import com.terramaster.utils.BundleParamsKey;
import com.terramaster.utils.IconUtils;
import com.terramaster.utils.LogM;
import com.terramaster.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class SelectPhotoAlbumActivity extends BaseActivity implements OnItemClickListener {
    private final int REQUEST_BACKUP_MODE = 1023;

    private HashMap<String, AlbumDetail> hashMapSelected = new HashMap<>();
    private ArrayList<AlbumDetail> albumDetailList = new ArrayList<>();

    private ListView listView;
    private View tvEmtyData;

    private SelectPhotoAlbumAdapter adapter;

    private ArrayList<AlbumBackupDetail> albumAlreadySelectedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo_album);

        initObjects();
        albumAlreadySelectedList = dbHelper.getAllAlbumBackupList(prefUtils.getDeviceData().getMac());

        findAllPhotoAlbum();
        updateAdapter();
        if (albumDetailList != null) {
            for (AlbumDetail album : albumDetailList) {
                LogM.e("PA:  " + album.path + " No: " + album.images.size());
                LogM.i("Image 1 " + album.images.get(0));
            }
        }
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        setTitle(R.string.select_album);
        enableBackButton();

        findViewById(R.id.btnNext).setOnClickListener(this);

        tvEmtyData = findViewById(R.id.tvEmtyData);

        listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.tvBackButton:
                onBackPressed();
                break;
            case R.id.btnNext:
                if (hashMapSelected.size() <= 0) {
                    AlertUtils.showSimpleAlert(SelectPhotoAlbumActivity.this, getString(R.string.e_select_album_for_backup));
                } else {
                    startActivityForResult(new Intent(SelectPhotoAlbumActivity.this, BackupModeActivity.class), REQUEST_BACKUP_MODE);
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void updateAdapter() {
        if (adapter == null) {
            adapter = new SelectPhotoAlbumAdapter(SelectPhotoAlbumActivity.this, albumDetailList, tvEmtyData);
            listView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void findAllPhotoAlbum() {
        albumDetailList.clear();

        Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaColumns.DATA, MediaColumns.DISPLAY_NAME};

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);

        int column_displayname = cursor.getColumnIndexOrThrow(MediaColumns.DISPLAY_NAME);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String imagePath = cursor.getString(column_index);
                String imageName = cursor.getString(column_displayname);

                int lastIndex = imagePath.lastIndexOf(imageName);

                lastIndex = lastIndex >= 0 ? lastIndex : imageName.length() - 1;

                String albumPath = imagePath.substring(0, lastIndex);

                if (imagePath != null && albumPath != null) {
                    addAlbum(albumPath, imagePath);
                }

            }
            cursor.close();
        }
    }

    private void addAlbum(String albumPath, String imagePath) {
        if (!StringUtils.isEmpty(albumPath)) {
            boolean isFound = false;
            for (AlbumDetail album : albumDetailList) {
                if (album.path.equals(albumPath)) {
                    isFound = true;
                    if (album.images.size() < 4) {
                        album.images.add(imagePath);
                    }
                    album.noOfPhotos++;
                    break;
                }
            }
            if (!isFound) {
                AlbumDetail albumDetail = new AlbumDetail(albumPath, imagePath);
                albumDetailList.add(albumDetail);
                if (isAlbumAlreadySelect(albumPath)) {
                    toggleSelection(albumDetail);
                }
            }
        }
    }

    private boolean isAlbumAlreadySelect(String albumPath) {
        if (albumAlreadySelectedList == null || albumAlreadySelectedList.size() <= 0) {
            return false;
        }
        for (AlbumBackupDetail album : albumAlreadySelectedList) {
            if (album.getFrom().equals(albumPath)) {
                return true;
            }
        }
        return false;
    }

    private AlbumBackupDetail removeAlbumFromList(String albumPath) {
        if (albumAlreadySelectedList == null || albumAlreadySelectedList.size() <= 0) {
            return null;
        }
        for (AlbumBackupDetail album : albumAlreadySelectedList) {
            if (album.getFrom().equals(albumPath)) {
                albumAlreadySelectedList.remove(album);
                return album;
            }
        }
        return null;
    }

    public boolean isSelected(AlbumDetail albumDetail) {
        String key = albumDetail.path;
        return hashMapSelected.containsKey(key);
    }

    public void toggleSelection(AlbumDetail albumDetail) {
        String key = albumDetail.path;
        if (hashMapSelected.containsKey(key)) {
            hashMapSelected.remove(key);
        } else {
            hashMapSelected.put(key, albumDetail);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        // TODO Auto-generated method stub
        AlbumDetail item = adapter.getItem(position);

        toggleSelection(item);

        updateAdapter();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_BACKUP_MODE && resultCode == RESULT_OK) {
            final boolean isBackupAllPhotos = data.getBooleanExtra(BundleParamsKey.IS_BACKUP_ALL_PHOTOS_KEY, true);
            final DeviceData deviceData = prefUtils.getDeviceData();

            new WorkerThread(SelectPhotoAlbumActivity.this, true) {

                @Override
                public String onWorkInBackground() {
                    // TODO Auto-generated method stub
                    Set<String> keySets = hashMapSelected.keySet();
                    for (String key : keySets) {
                        AlbumDetail albumDetail = hashMapSelected.get(key);

                        AlbumBackupDetail autoBackup = removeAlbumFromList(albumDetail.path);
                        if (autoBackup == null) {
                            autoBackup = new AlbumBackupDetail();
                        }
                        autoBackup.setFrom(albumDetail.path);
                        Date date = new Date();
                        autoBackup.setLastUpdated(date);
                        autoBackup.setCreatedAt(date);
                        autoBackup.setBakupMode(isBackupAllPhotos ? DBKeys.MODE_BACKUP_ALL : DBKeys.MODE_BACKUP_ONLY_NEW);
                        autoBackup.setNoOfBackupPhotos(albumDetail.noOfPhotos);
                        autoBackup.setMacAddress(deviceData.getMac());
                        autoBackup.setEnabled(true);

                        if (autoBackup.getRowId() > 0) {
                            dbHelper.updateAlbumBackup(autoBackup);
                        } else {
                            long rowId = dbHelper.insertAlbumBackup(autoBackup);
                            autoBackup.setRowId(rowId);
                        }

                        File[] allImages = new File(autoBackup.getFrom()).listFiles();
                        for (File file : allImages) {
                            if (!IconUtils.isPhoto(file.getName())) {
                                continue;
                            }
                            ImageBackupDetail detail = dbHelper.getImageBackup(autoBackup.getRowId(), file.getAbsolutePath(), deviceData.getMac());
                            if (detail != null) {
                                if (!isBackupAllPhotos) {
                                    detail.setTaskStatus(DBKeys.STATUS_SKIPED);
                                    dbHelper.updateImageBackupDetail(detail);
                                } else {
                                    if (detail.getTaskStatus() == DBKeys.STATUS_SKIPED || detail.getTaskStatus() == DBKeys.STATUS_PAUSED || detail.getTaskStatus() == DBKeys.STATUS_ERROR) {
                                        detail.setTaskStatus(DBKeys.STATUS_WAITING);
                                        dbHelper.updateImageBackupDetail(detail);
                                    }
                                }

                            } else {
                                if (!isBackupAllPhotos) {
                                    detail = new ImageBackupDetail();
                                    detail.setFrom(file.getAbsolutePath());
                                    detail.setAlbumId(autoBackup.getRowId());
                                    detail.setLastUpdated(date);
                                    detail.setCreatedAt(date);
                                    detail.setTaskStatus(DBKeys.STATUS_SKIPED);
                                    detail.setMacAddress(deviceData.getMac());

                                    long id = dbHelper.insertImageBackup(detail);
                                    detail.setRowId(id);
                                }
                            }
                        }

                    }

                    if (albumAlreadySelectedList != null && albumAlreadySelectedList.size() > 0) {
                        for (AlbumBackupDetail album : albumAlreadySelectedList) {
                            album.setEnabled(false);
                            dbHelper.updateAlbumBackup(album);
                        }
                    }

                    return null;
                }

                @Override
                protected synchronized void onWorkFinished(String result) {
                    super.onWorkFinished(result);

                    prefUtils.setAutoBackup(true);
                    Intent intent = new Intent(Intent.ACTION_SYNC, null, SelectPhotoAlbumActivity.this, AutoBackupService.class);
                    startService(intent);

                    setResult(RESULT_OK);
                    finish();
                }

                ;
            }.execute();

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
