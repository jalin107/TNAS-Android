package com.terramaster.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.terramaster.LocalFilesActivity;
import com.terramaster.R;
import com.terramaster.adapter.LocalFilesAdapter;
import com.terramaster.model.FileItem;
import com.terramaster.task.WorkerThread;
import com.terramaster.utils.AlertUtils;
import com.terramaster.utils.SDCardUtils;
import com.terramaster.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalFilesFragment extends BaseFragment implements OnItemClickListener {
    private View contentView;
    private LocalFilesActivity mActivity;

    private FileItem currentDirectory;
    private LocalFilesAdapter adapter;
    private ListView listView;
    private View tvEmtyData, progressBar;

    private List<FileItem> fileItems;

    public LocalFilesFragment(FileItem item) {
        // TODO Auto-generated constructor stub
        currentDirectory = item;
        if (currentDirectory == null) {
            currentDirectory = new FileItem();
            currentDirectory.setFile(Environment.getExternalStorageDirectory());
        }
    }

    public void showProgressBar(boolean isShow) {
        progressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
        listView.setVisibility(isShow ? View.GONE : View.VISIBLE);
        if (isShow && tvEmtyData.getVisibility() == View.VISIBLE) {
            tvEmtyData.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (contentView == null) {
            mActivity = (LocalFilesActivity) getActivity();
            contentView = inflater.inflate(R.layout.fragment_local_files, container, false);
            initObjects(contentView);

            fetchAllFiles();
        } else {
            ViewGroup parentViewGroup = (ViewGroup) contentView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeView(contentView);
            }

        }
        return contentView;
    }

    private void initObjects(View v) {
        // TODO Auto-generated method stub
        tvEmtyData = v.findViewById(R.id.tvEmtyData);
        progressBar = v.findViewById(R.id.progressBar);

        listView = (ListView) v.findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);
    }

    private void fetchAllFiles() {
        showProgressBar(true);
        new WorkerThread(mActivity) {

            @Override
            public String onWorkInBackground() {
                // TODO Auto-generated method stub
                fileItems = new ArrayList<FileItem>();
                try {
                    File[] dirs = currentDirectory.getFile().listFiles();

                    for (File ff : dirs) {
                        if (ff.getName().startsWith(".")) {
                            continue;
                        }
                        FileItem fileItem = new FileItem();
                        fileItem.setFile(ff);

                        fileItems.add(fileItem);
                    }

                    Collections.sort(fileItems);
                    return "0";
                } catch (Exception e) {
                    // TODO Auto-generated catch block

                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected synchronized void onWorkFinished(String result) {
                // TODO Auto-generated method stub
                super.onWorkFinished(result);
                showProgressBar(false);
                if (result != null) {

                } else {

                }

                updateAdapter();
            }
        }.start();
    }

    private void updateAdapter() {
        if (adapter == null) {
            adapter = new LocalFilesAdapter(mActivity, fileItems);
            listView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        tvEmtyData.setVisibility(fileItems.size() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        // TODO Auto-generated method stub
        FileItem item = adapter.getItem(position);
        if (item.isDirectory()) {
            mActivity.pushFragment(new LocalFilesFragment(item), true, true);
        } else {
            onFileClick(item);
        }
    }

    private void onFileClick(FileItem item) {
        final String extension = StringUtils.getExtensionFromPath(item.getPath());
        if (extension == null) {
            AlertUtils.showSimpleAlert(mActivity, getString(R.string.e_unknown_file_format));
            return;
        }
        SDCardUtils.openFile(mActivity, item.getFile());
    }

}
