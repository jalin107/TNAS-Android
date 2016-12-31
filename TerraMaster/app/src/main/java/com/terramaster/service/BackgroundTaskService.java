package com.terramaster.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.terramaster.dbhelper.DBKeys;
import com.terramaster.dbhelper.DatabaseHelper;
import com.terramaster.discover.DeviceData;
import com.terramaster.ftp.FTPHelper;
import com.terramaster.model.TaskDetail;
import com.terramaster.task.WorkerThread;
import com.terramaster.utils.BundleParamsKey;
import com.terramaster.utils.SDCardUtils;
import com.terramaster.utils.SharedPrefUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BackgroundTaskService extends Service {
    public static final String UPDATE_RECEIVER = "com.terramaster.ACTION_UPDATE";
    private FTPHelper ftpHelper;
    private DatabaseHelper dbHelper;
    private SharedPrefUtils prefUtils;

    private HashMap<Long, ExecuteTask> allTasks = new HashMap<>();
    private HashMap<Long, String> meTaskStatus = new HashMap<>();
    private DeviceData deviceData;
    private boolean isLoginInProgress = false;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private ArrayList<TaskDetail> pendingTasks = new ArrayList<>();

    public BackgroundTaskService() {
        // TODO Auto-generated constructor stub
        showLog("Service inited");
    }

    private void showLog(String msg) {
        Log.i("tasks", msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        showLog("Service onStartCommand");
        if(prefUtils==null) {
            prefUtils = SharedPrefUtils.getInstance(BackgroundTaskService.this);
        }
        if(deviceData==null) {
            deviceData = prefUtils.getDeviceData();
        }
        if(dbHelper==null) {
            dbHelper = DatabaseHelper.getInstance(BackgroundTaskService.this);
        }
        if(ftpHelper==null) {
            ftpHelper = new FTPHelper();
        }

        int action = intent.getIntExtra(BundleParamsKey.SERVICE_ACTION_KEY, BundleParamsKey.START_ACTION);

        final TaskDetail taskDetail = (TaskDetail) intent.getSerializableExtra(BundleParamsKey.OBJECT_KEY);
        if (action == BundleParamsKey.START_ACTION) {
            checkLoginAndExecutePendingTasks(taskDetail);
        } else if (action == BundleParamsKey.PAUSE_ACTION) {
            pauseRunningTask(taskDetail);
        }else if(action == BundleParamsKey.PAUSEALL_ACTION){
            pauseAllRunningTask(taskDetail);
        }else if(action == BundleParamsKey.DELTE_ACTION){
            Log.e("Error", "BundleParamsKey.DELTE_ACTION");
            removeRunningTask(taskDetail);
        }
        return START_REDELIVER_INTENT;
    }

    private boolean isSameTaskRunning(int taskType) {
        boolean isRunning = allTasks.size() > 0;
    //	Set<Long> keySets = allTasks.keySet();
    //	for (Long key : keySets)
    //	{
    //	    TaskDetail taskDetail = allTasks.get(key).taskDetail;
    //	    if (taskDetail.getTaskType() == taskType)
    //	    {
    //		isRunning = true;
    //		break;
    //	    }
    //	}
        return isRunning;
    }

    private void removeRunningTask(TaskDetail taskDetail) {
        ExecuteTask thread = allTasks.get(taskDetail.getRowId());
        if (thread != null) {
            thread.cancel(true);
            allTasks.remove(taskDetail.getRowId());
        }
        String access = meTaskStatus.get(taskDetail.getRowId());
        if(access != null) meTaskStatus.remove(taskDetail.getRowId());
        taskDetail.setTaskStatus(DBKeys.STATUS_PAUSED);
        updateTaskDetail(taskDetail);
        findNextTask(taskDetail);
    }

    private void pauseRunningTask(TaskDetail taskDetail) {
        ExecuteTask thread = allTasks.get(taskDetail.getRowId());
        if (thread != null) {
            thread.cancel(true);
            allTasks.remove(taskDetail.getRowId());
        }
        meTaskStatus.put(taskDetail.getRowId(), "pause");
        taskDetail.setTaskStatus(DBKeys.STATUS_PAUSED);
        updateTaskDetail(taskDetail);
        findNextTask(taskDetail);
    }

    private void pauseAllRunningTask(TaskDetail taskDetail) {
        ExecuteTask thread = allTasks.get(taskDetail.getRowId());
        if (thread != null) {
            thread.cancel(true);
            allTasks.remove(taskDetail.getRowId());
        }
        meTaskStatus.put(taskDetail.getRowId(), "pause");
        taskDetail.setTaskStatus(DBKeys.STATUS_PAUSED);
        updateTaskDetail(taskDetail);
    }

    private void checkLoginAndExecutePendingTasks(final TaskDetail taskDetail) {
        if (ftpHelper.isLogin()) {
            startTask(taskDetail);
        } else {
            if (!pendingTasks.contains(taskDetail)) {
                pendingTasks.add(taskDetail);
            }
            if (isLoginInProgress) { return;  }
            isLoginInProgress = true;
            new WorkerThread(BackgroundTaskService.this) {
                @Override
                public String onWorkInBackground() {
                    // TODO Auto-generated method stub
                    try {
                        String userName = prefUtils.getUsername();
                        String password = prefUtils.getPassword();
                        ftpHelper.logIn(deviceData.IPV4, deviceData.getDeviceService("ftp").port, userName, password);
                    }catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected synchronized void onWorkFinished(String result) {
                    // TODO Auto-generated method stub
                    super.onWorkFinished(result);
                    isLoginInProgress = false;
                    if (ftpHelper.isLogin()) {
                        showLog("Login success...Running all pending tasks");
                        for (TaskDetail task : pendingTasks) {
                            startTask(task);
                        }
                    } else {
                        checkLoginAndExecutePendingTasks(taskDetail);
                    }
                }
            }.start();
        }
    }

    private void startTask(final TaskDetail taskDetail) {
        if (isSameTaskRunning(taskDetail.getTaskType())) {
            showLog("Service Task skipped: " + taskDetail.getRowId());
            return;
        }
        if (taskDetail.getTaskStatus() == DBKeys.STATUS_WAITING){
            ExecuteTask thread = new ExecuteTask(BackgroundTaskService.this, taskDetail);
            thread.execute();
            allTasks.put(taskDetail.getRowId(), thread);
        } else {
            findNextTask(taskDetail);
        }
    }

    private void findNextTask(TaskDetail taskDoneDetail) {
        TaskDetail taskDetail = dbHelper.findWaitingTask(deviceData.getMac());
        if (taskDetail != null) {
            showLog("Service Next Task found: " + taskDetail.getRowId());
            startTask(taskDetail);
        }
    }
    //更新UI界面方法...
    private void updateTaskDetail(TaskDetail taskDetail) {
        taskDetail.setLastUpdated(new Date());
        dbHelper.updateTaskDetail(taskDetail);

        Intent intentBroadcats = new Intent();
        intentBroadcats.setAction(UPDATE_RECEIVER);
        intentBroadcats.putExtra(BundleParamsKey.OBJECT_KEY, taskDetail);
        sendBroadcast(intentBroadcats);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private class ExecuteTask extends WorkerThread {
        public TaskDetail taskDetail;
        String task_name = "";
        private FTPClient mFtpClient;

        public ExecuteTask(Context context, TaskDetail taskDetail) {
            super(context);
            // TODO Auto-generated constructor stub
            this.taskDetail = taskDetail;
        }

        @Override
        protected void onWorkStarted() {
            // TODO Auto-generated method stub
            super.onWorkStarted();
            taskDetail.setTaskStatus(DBKeys.STATUS_RUNNING);
            updateTaskDetail(taskDetail);

            if (taskDetail.getTaskType() == DBKeys.TASK_DOWNLOADING) {
                task_name = "Downloading ";
            } else if (taskDetail.getTaskType() == DBKeys.TASK_UPLOADING) {
                task_name = "Uploading ";
            }
            showLog("Service " + task_name + "task start: " + taskDetail.getRowId());
        }

        @Override
        public String onWorkInBackground() {
            // TODO Auto-generated method stub
            try {
                doTask(taskDetail);
            } catch (Exception e) {
                e.printStackTrace();
                taskDetail.setTaskStatus(DBKeys.STATUS_ERROR);
            }
            return null;
        }

        private void doTask(TaskDetail taskDetail) throws Exception {
            if (taskDetail.getTaskType() == DBKeys.TASK_DOWNLOADING) {
                File file = SDCardUtils.createDownloadingFile(taskDetail.getFrom());
                showLog("File name: " + file.getAbsolutePath());
                taskDetail.setTo(file.getAbsolutePath());
                mFtpClient = ftpHelper.getmFtpClient();
                long lRemoteSize = taskDetail.getTaskFileSize();
                File localfile = new File(taskDetail.getTo());
                String remotefile = taskDetail.getFrom();
                FileOutputStream out = null;
                long localSize = 0L;
                long process = 0;
                long step = lRemoteSize /100;
                if(localfile.exists()){//进行断点续传，并记录状态
                    localSize = localfile.length();
                    process = localSize /step;
                    //判断本地文件大小是否大于远程文件大小
                    if(localSize >= lRemoteSize){
                        System.out.println("本地文件大于远程文件，下载中止");
                        taskDetail.setTaskStatus(DBKeys.STATUS_COMPLETE);
                        return;
                    }else{
                        out = new FileOutputStream(localfile, true);
                        mFtpClient.setRestartOffset(localSize);
                    }
                }else {
                    out = new FileOutputStream(localfile);
                }
                InputStream input = mFtpClient.retrieveFileStream(remotefile);
                byte[] bytes = new byte[1024];
                int c;
                while(!this.isCancelled() && (c = input.read(bytes))!= -1){
                    out.write(bytes,0, c);
                    localSize += c;
                    long nowProcess = localSize /step;
                    if(nowProcess > process){
                        process = nowProcess;
                        taskDetail.setProcess(process);
                        updateTaskDetail(taskDetail);
                    }
                }
                out.flush();
                out.close();
                input.close();
                boolean isDo = mFtpClient.completePendingCommand();
                if (isDo){
                    taskDetail.setTaskStatus(DBKeys.STATUS_COMPLETE);
                }else{
                    mFtpClient.logout();
                    mFtpClient.disconnect();
                    taskDetail.setTaskStatus(DBKeys.STATUS_ERROR);
                }
            } else if (taskDetail.getTaskType() == DBKeys.TASK_UPLOADING) {
                File file = new File(taskDetail.getFrom());
                //Jaylin upload folder
                boolean success = true;
                if(file.isDirectory()){
                    success = ftpHelper.uploadDirectory(taskDetail.getFrom(), taskDetail.getTo());
                }else {
                    success = ftpHelper.uploadFileToFolder(file, taskDetail.getTo());
                }
                if (success) {
                    taskDetail.setTaskStatus(DBKeys.STATUS_COMPLETE);
                } else {
                    taskDetail.setTaskStatus(DBKeys.STATUS_ERROR);
                }
            }
        }

        @Override
        protected synchronized void onWorkCancelled(String result) {
            // TODO Auto-generated method stub
            super.onWorkCancelled(result);
            if (taskDetail.getTaskType() == DBKeys.TASK_DOWNLOADING) {
                File file = SDCardUtils.createDownloadingFile(taskDetail.getFrom());
                String access = meTaskStatus.get(taskDetail.getRowId());
                if (file.exists() && access == null) {
                    file.delete();
                }
            } else if (taskDetail.getTaskType() == DBKeys.TASK_UPLOADING) {
                new WorkerThread(mContext) {
                    @Override
                    public String onWorkInBackground() {
                        // TODO Auto-generated method stub
                        try {
                            ftpHelper.deleteFile(taskDetail.getTo());
                        } catch (Exception e) {

                        }
                        return null;
                    }
                }.execute();
            }
        }

        @Override
        protected synchronized void onWorkFinished(String result) {
            // TODO Auto-generated method stub
            super.onWorkFinished(result);
            updateTaskDetail(taskDetail);
            allTasks.remove(taskDetail.getRowId());
            showLog("Service " + task_name + "task end: " + taskDetail.getRowId());
            findNextTask(taskDetail);
        }
    }

}
