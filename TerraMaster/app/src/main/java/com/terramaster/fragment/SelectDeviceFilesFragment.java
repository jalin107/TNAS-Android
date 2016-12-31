package com.terramaster.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.terramaster.R;
import com.terramaster.SelectDeviceFilesActivity;
import com.terramaster.adapter.SelectDeviceFilesAdapter;
import com.terramaster.model.FTPFileItem;
import com.terramaster.task.WorkerThread;

import java.util.ArrayList;
import java.util.List;

public class SelectDeviceFilesFragment extends BaseFragment implements OnItemClickListener {
    private View contentView;
    private SelectDeviceFilesActivity mActivity;

    private FTPFileItem currentFile;
    private SelectDeviceFilesAdapter adapter;
    private ListView listView;
    private View tvEmtyData, progressBar;

    private List<FTPFileItem> ftpFileItems;

    public SelectDeviceFilesFragment(FTPFileItem item) {
        // TODO Auto-generated constructor stub
        currentFile = item;
    }

    public FTPFileItem getFTPFileItem() {
        return currentFile;
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
            mActivity = (SelectDeviceFilesActivity) getActivity();
            contentView = inflater.inflate(R.layout.fragment_device_files, container, false);
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

    @Override
    public void reloadData() {
        // TODO Auto-generated method stub
        super.reloadData();

        fetchAllFiles();
    }

    private void fetchAllFiles() {
        showProgressBar(true);
        new WorkerThread(mActivity) {

            @Override
            public String onWorkInBackground() {
                // TODO Auto-generated method stub
                try {
                    ftpFileItems = mActivity.ftpHelper.fetchAllFiles(currentFile, mActivity.prefUtils.getLoginDeviceName());
                    return "0";
                } catch (Exception e) {

                    Log.e("Select Device files"," ....login()");
                    mActivity.ftpHelper.logIn();

                    // TODO Auto-generated catch block
                    ftpFileItems = new ArrayList<FTPFileItem>();
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
            adapter = new SelectDeviceFilesAdapter(mActivity, ftpFileItems, tvEmtyData);
            listView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged(ftpFileItems);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        // TODO Auto-generated method stub
        FTPFileItem item = adapter.getItem(position);
        if (item.isDirectory()) {
            mActivity.pushFragment(new SelectDeviceFilesFragment(item), true, true);
        } else {
            mActivity.toggleSelection(item);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            } else {
                updateAdapter();
            }
        }
    }

}
