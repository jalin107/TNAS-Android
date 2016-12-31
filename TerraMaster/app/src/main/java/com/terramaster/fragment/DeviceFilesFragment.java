package com.terramaster.fragment;

import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.terramaster.DeviceFilesActivity;
import com.terramaster.R;
import com.terramaster.adapter.DeviceFilesAdapter;
import com.terramaster.discover.DeviceData;
import com.terramaster.discover.DeviceService;
import com.terramaster.http.VolleySingle;
import com.terramaster.model.FTPFileItem;
import com.terramaster.model.FileItem;
import com.terramaster.player.MyImageView;
import com.terramaster.task.CacheFTPFileTask;
import com.terramaster.task.OnCacheFileListener;
import com.terramaster.task.WorkerThread;
import com.terramaster.utils.AlertUtils;
import com.terramaster.utils.IconUtils;

import com.terramaster.utils.SDCardUtils;
import com.terramaster.utils.SharedPrefUtils;
import com.terramaster.utils.StringUtils;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceFilesFragment extends BaseFragment implements OnItemClickListener {
    private View contentView;
    private DeviceFilesActivity mActivity;
    private String PHPSESSID = "";
    private FTPFileItem currentDirectory;
    private DeviceFilesAdapter adapter;
    private ListView listView;
    private View tvEmtyData, progressBar;
    private SharedPrefUtils prefUtils;
    private DeviceData dd;
    private DeviceService ds;

    private List<FTPFileItem> ftpFileItems;

    public DeviceFilesFragment()
    {

    }
    public DeviceFilesFragment(FTPFileItem item) {
        // TODO Auto-generated constructor stub
        currentDirectory = item;
        prefUtils = SharedPrefUtils.getInstance(this.getContext());
        dd = prefUtils.getDeviceData();
        ds = dd.getDeviceService("sys");
        PHPSESSID = prefUtils.getSession();
    }

    public FTPFileItem getFTPFileItem() {
        return currentDirectory;
    }

    public void showProgressBar(boolean isShow) {
        progressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
        listView.setVisibility(isShow ? View.GONE : View.VISIBLE);
        if (isShow && tvEmtyData.getVisibility() == View.VISIBLE) {
            tvEmtyData.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (contentView == null) {
            mActivity = (DeviceFilesActivity) getActivity();
            contentView = inflater.inflate(R.layout.fragment_device_files, container, false);
            initObjects(contentView);

            fetchAllFiles();
        } else {
            ViewGroup parentViewGroup = (ViewGroup) contentView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeView(contentView);
            }
        }
        mActivity.setTitle(currentDirectory != null ? currentDirectory.getName() : mActivity.prefUtils.getDeviceData().HOSTNAME);
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
// andsontan
    private void fetchAllFiles() {
        showProgressBar(true);
        new WorkerThread(mActivity) {

            @Override
            public String onWorkInBackground() {
                // TODO Auto-generated method stub
                try {
                    ftpFileItems = mActivity.ftpHelper.fetchAllFiles(currentDirectory, mActivity.prefUtils.getLoginDeviceName());
                    return "0";
                } catch (Exception e) {
                    // TODO Auto-generated catch block

                    Log.e(" Device files"," ....login()");
                    mActivity.ftpHelper.logIn();

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
            adapter = new DeviceFilesAdapter(mActivity, ftpFileItems, tvEmtyData);
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
            mActivity.pushFragment(new DeviceFilesFragment(item), true, true);
        } else {
            onFileClick(item);
        }
    }

    private String _requestURI(String urladdr, final FTPFileItem file, final String fileType)
    {
        final String result = null;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urladdr, new Response.Listener<String>(){
            @Override
            public void onResponse(String s){
                //Log.e("volley",s);
                if(s.length() == 32){
                    PHPSESSID = s;
                    dd.PHPSESSID = s;
                    prefUtils.setDeviceData(dd, mActivity.ftpHelper.xuserName, mActivity.ftpHelper.xpass);
                }
                String path = AddPrePath(file);
                //http://10.18.13.9:8181/media_team_play/258c5caac9784c07ee8e49a23eff7d86/mnt/public/彩云追月.mp3
                String URL = "http://"+mActivity.ftpHelper.xip+":"+ds.port+"/media_team_play/"+PHPSESSID+path;
                Intent intent;
                if(fileType.equals("image/*")){
                    //Jaylin... for all photos
                    URL = "http://"+mActivity.ftpHelper.xip+":"+ds.port+"/media_team_play/"+PHPSESSID;
                    ArrayList<String> urls = new ArrayList<String>();
                    ArrayList<String> names = new ArrayList<String>();
                    for(FTPFileItem tmpFile : ftpFileItems){
                        if (!tmpFile.isDirectory() && IconUtils.isPhoto(tmpFile.getName())){
                            String tmppath = AddPrePath(tmpFile,true);
                            String url = URL + tmppath;
                            urls.add(url);
                            names.add(tmpFile.getName());
                        }
                    }
                    intent = new Intent(mActivity, MyImageView.class);
                    intent.putStringArrayListExtra("URLS", urls);
                    intent.putStringArrayListExtra("NAMES", names);
                    intent.putExtra("MYFILE", file.getName());
                    startActivity(intent);
                }else {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(URL), fileType);
                    startActivity(intent);
                }
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("volleyerror","erro2");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("name", mActivity.ftpHelper.xuserName);
                map.put("password", mActivity.ftpHelper.xpass);
                map.put("PHPSESSID", PHPSESSID);
                map.put("platform", "android");
                return map;
            }
        };
        VolleySingle.getVolleySingle(mActivity).addToRequestQueue(stringRequest);
        return result;
    }

    private String AddPrePath(FTPFileItem file){
        String path = "/mnt/"+file.getPath();
        if(file.getPath().startsWith(mActivity.ftpHelper.xuserName+"/"))
            path = "/home/"+file.getPath();
        return StringUtils._ReplaceAllUrl(path);
    }

    private String AddPrePath(FTPFileItem file, boolean encode){
        String path = "/mnt/"+file.getPath();
        if(file.getPath().startsWith(mActivity.ftpHelper.xuserName+"/"))
            path = "/home/"+file.getPath();
        return URLEncoder.encode(path);
    }

    private void onFileClick(FTPFileItem item) {
        final String extension = StringUtils.getExtensionFromPath(item.getPath());
        if (extension == null) {
            AlertUtils.showSimpleAlert(mActivity, getString(R.string.e_unknown_file_format));
            return;
        }
        Log.e("xxxxpalyfile",item.getPath());
        String fileName = item.getName();
        String url = "http://"+mActivity.ftpHelper.xip+":"+ds.port+"/3.0/index.php?user/loginSubmit";
        if(IconUtils.isVideo(fileName)){
            _requestURI(url, item, "video/*");
        }else if(IconUtils.isAudio(fileName)){
            _requestURI(url, item, "audio/*");
        }else if(IconUtils.isPhoto(fileName)){
            _requestURI(url, item, "image/*");
        }else{
            new CacheFTPFileTask(mActivity, item, new OnCacheFileListener() {
                @Override
                public void onSuccess(File file) {
                    // TODO Auto-generated method stub
                    Log.e("xxxxpalyfile",file.getName());
                    SDCardUtils.openFile(mActivity, file);
                }
            }).execute();
        }
    }

}
