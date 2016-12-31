package com.terramaster;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.terramaster.adapter.TaskListAdapter;
import com.terramaster.dbhelper.DBKeys;
import com.terramaster.dialog.ShareDialog;
import com.terramaster.dialog.SuccessToast;
import com.terramaster.discover.DeviceData;
import com.terramaster.discover.DeviceService;
import com.terramaster.model.TaskDetail;
import com.terramaster.service.BackgroundTaskService;
import com.terramaster.task.OnTaskOptionListener;
import com.terramaster.utils.AlertUtils;
import com.terramaster.utils.BundleParamsKey;
import com.terramaster.utils.SDCardUtils;
import com.terramaster.utils.StringUtils;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends BaseActivity implements OnItemClickListener {
    private ListView listView;
    private TaskListAdapter adapter;

    private View tvEmtyData;

    private DeviceData deviceData;
    private OnTaskOptionListener onTaskOptionListener = new OnTaskOptionListener() {

        @Override
        public void onSelect(TaskDetail taskDetail, int itemId) {
            // TODO Auto-generated method stub
            switch (itemId) {
//                case R.id.tvShare:
//                case R.id.tvShare2:
//
//                    ArrayList<String> list = new ArrayList<>();
//                    list.add(taskDetail.getFrom());
//                    DeviceData deviceData= prefUtils.getDeviceData();
//                    DeviceService deviceServiceSYC=deviceData==null?null:deviceData.getDeviceService("sys");
//                    ShareDialog.startShareLinks(TaskListActivity.this,""+(deviceServiceSYC==null?"":deviceServiceSYC.port), list);
//                    break;
                case R.id.tvDelete:
                case R.id.tvDelete2:
                    if (taskDetail.getTaskStatus() == DBKeys.STATUS_RUNNING) {
                        Intent intentDelete = new Intent(Intent.ACTION_SYNC, null, TaskListActivity.this, BackgroundTaskService.class);
                        intentDelete.putExtra(BundleParamsKey.OBJECT_KEY, taskDetail);
                        intentDelete.putExtra(BundleParamsKey.SERVICE_ACTION_KEY, BundleParamsKey.DELTE_ACTION);
                        startService(intentDelete);
                    }
                    dbHelper.deleteTask(taskDetail.getRowId());
                    updateAdapter();
                    new SuccessToast(TaskListActivity.this, getString(R.string.success_delete_task)).show();
                    break;
                case R.id.tvPlayPause:
                    if (taskDetail.getTaskStatus() == DBKeys.STATUS_RUNNING) {
                        Intent intent = new Intent(Intent.ACTION_SYNC, null, TaskListActivity.this, BackgroundTaskService.class);
                        intent.putExtra(BundleParamsKey.OBJECT_KEY, taskDetail);
                        intent.putExtra(BundleParamsKey.SERVICE_ACTION_KEY, BundleParamsKey.PAUSE_ACTION);
                        startService(intent);
                        new SuccessToast(TaskListActivity.this, getString(R.string.success_pause_task)).show();
                    } else {
                        taskDetail.setTaskStatus(DBKeys.STATUS_WAITING);
                        dbHelper.updateTaskDetail(taskDetail);
                        updateAdapter();
                        Intent intent = new Intent(Intent.ACTION_SYNC, null, TaskListActivity.this, BackgroundTaskService.class);
                        intent.putExtra(BundleParamsKey.OBJECT_KEY, taskDetail);
                        intent.putExtra(BundleParamsKey.SERVICE_ACTION_KEY, BundleParamsKey.START_ACTION);
                        startService(intent);
                        new SuccessToast(TaskListActivity.this, getString(R.string.success_recover_task)).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            updateAdapter();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        initObjects();

        updateAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BackgroundTaskService.UPDATE_RECEIVER);
        registerReceiver(mReceiver, filter);

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        setTitle(getString(R.string.tasks_list));
        enableBackButton();

        deviceData = prefUtils.getDeviceData();

        tvEmtyData = findViewById(R.id.tvEmtyData);

        listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);

        findViewById(R.id.tvHome).setOnClickListener(this);
        findViewById(R.id.tvPauseAll).setOnClickListener(this);
        findViewById(R.id.tvRecoverAll).setOnClickListener(this);
        findViewById(R.id.tvDeleteAll).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.tvBackButton:
                onBackPressed();
                break;
            case R.id.tvHome:
                onBackPressed();
                break;
            case R.id.tvPauseAll:
                pauseAll();
                break;
            case R.id.tvRecoverAll:
                recoverAll();
                break;
            case R.id.tvDeleteAll:
                if (adapter.getCount() <= 0) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskListActivity.this);
                builder.setIcon(0);
                builder.setTitle("");
                View view = LayoutInflater.from(TaskListActivity.this).inflate(R.layout.dialog_alert, null, false);
                builder.setView(view);

                final AlertDialog dialog = builder.create();
                TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
                tvMessage.setText(getString(R.string.confirm_delete_all_task));
                Button btnPositive = (Button) view.findViewById(R.id.btnPositive);
                btnPositive.setVisibility(View.VISIBLE);
                btnPositive.setText(getString(R.string.all));
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        deleteAll();
                    }
                });
                Button btnNeutral = (Button) view.findViewById(R.id.btnNeutral);
                btnNeutral.setVisibility(View.VISIBLE);
                btnNeutral.setText(getString(R.string.all_completed));
                btnNeutral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        deleteAllCompleted();
                    }
                });

                Button btnNegative = (Button) view.findViewById(R.id.btnNegative);
                btnNegative.setVisibility(View.VISIBLE);
                btnNegative.setText(getString(R.string.cancel));
                btnNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void pauseAll() {
        List<TaskDetail> taskList = dbHelper.getAllTasks(deviceData.getMac());
        if (taskList.size() <= 0) {
            return;
        }
        List<TaskDetail> runningTaskList = new ArrayList<TaskDetail>();
        for (TaskDetail taskDetail : taskList) {
            if (taskDetail.getTaskStatus() == DBKeys.STATUS_RUNNING) {
                runningTaskList.add(taskDetail);
            } else if (taskDetail.getTaskStatus() == DBKeys.STATUS_WAITING) {
                taskDetail.setTaskStatus(DBKeys.STATUS_PAUSED);
                dbHelper.updateTaskDetail(taskDetail);
            }
        }
        updateAdapter();
        for (TaskDetail taskDetail : runningTaskList) {
            if (taskDetail.getTaskStatus() == DBKeys.STATUS_RUNNING) {
                Intent intent = new Intent(Intent.ACTION_SYNC, null, TaskListActivity.this, BackgroundTaskService.class);
                intent.putExtra(BundleParamsKey.OBJECT_KEY, taskDetail);
                intent.putExtra(BundleParamsKey.SERVICE_ACTION_KEY, BundleParamsKey.PAUSEALL_ACTION);
                startService(intent);
            }
        }
        new SuccessToast(TaskListActivity.this, getString(R.string.success_pause_all_task)).show();
    }

    private void recoverAll() {
        boolean isRunning = false;
        List<TaskDetail> taskList = dbHelper.getAllTasks(deviceData.getMac());
        if (taskList.size() <= 0) {
            return;
        }
        for (TaskDetail taskDetail : taskList) {
            if (taskDetail.getTaskStatus() == DBKeys.STATUS_RUNNING) {
                isRunning = true;
            } else if (taskDetail.getTaskStatus() == DBKeys.STATUS_ERROR || taskDetail.getTaskStatus() == DBKeys.STATUS_PAUSED) {
                taskDetail.setTaskStatus(DBKeys.STATUS_WAITING);
                dbHelper.updateTaskDetail(taskDetail);
            }
        }
        updateAdapter();
        if (!isRunning){
            TaskDetail taskDetail = dbHelper.findWaitingTask(deviceData.getMac());
            if (taskDetail != null) {
                Intent intent = new Intent(Intent.ACTION_SYNC, null, TaskListActivity.this, BackgroundTaskService.class);
                intent.putExtra(BundleParamsKey.OBJECT_KEY, taskDetail);
                intent.putExtra(BundleParamsKey.SERVICE_ACTION_KEY, BundleParamsKey.START_ACTION);
                startService(intent);
            }
        }
        new SuccessToast(TaskListActivity.this, getString(R.string.success_recover_all_task)).show();
    }

    private void deleteAll() {
        List<TaskDetail> taskList = dbHelper.getAllTasks(deviceData.getMac());
        for (TaskDetail taskDetail : taskList) {
            if (taskDetail.getTaskStatus() == DBKeys.STATUS_RUNNING) {
                Intent intentDelete = new Intent(Intent.ACTION_SYNC, null, TaskListActivity.this, BackgroundTaskService.class);
                intentDelete.putExtra(BundleParamsKey.OBJECT_KEY, taskDetail);
                intentDelete.putExtra(BundleParamsKey.SERVICE_ACTION_KEY, BundleParamsKey.DELTE_ACTION);
                startService(intentDelete);
            }
        }
        dbHelper.deleteAllTask();
        updateAdapter();
        new SuccessToast(TaskListActivity.this, getString(R.string.success_delete_all_task)).show();
    }

    private void deleteAllCompleted() {
        dbHelper.deleteAllCompletedTask();
        updateAdapter();
        new SuccessToast(TaskListActivity.this, getString(R.string.success_delete_all_completed_task)).show();
    }

    private void updateAdapter() {
        List<TaskDetail> taskList = dbHelper.getAllTasks(deviceData.getMac());
        if (adapter == null) {
            adapter = new TaskListAdapter(TaskListActivity.this, taskList, tvEmtyData, onTaskOptionListener);
            listView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged(taskList);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
        // TODO Auto-generated method stub
        TaskDetail item = adapter.getItem(position);
        if (item.getTaskStatus() == DBKeys.STATUS_COMPLETE) {
            String path = null;
            if (item.getTaskType() == DBKeys.TASK_UPLOADING) {
                path = item.getFrom();
            } else if (item.getTaskType() == DBKeys.TASK_DOWNLOADING) {
                path = item.getTo();
            }
            if (!StringUtils.isEmpty(path)) {
                final String extension = StringUtils.getExtensionFromPath(path);
                if (extension == null) {
                    AlertUtils.showSimpleAlert(TaskListActivity.this, getString(R.string.e_unknown_file_format));
                    return;
                }
                SDCardUtils.openFile(TaskListActivity.this, new File(path));

            }
        }
    }

}
