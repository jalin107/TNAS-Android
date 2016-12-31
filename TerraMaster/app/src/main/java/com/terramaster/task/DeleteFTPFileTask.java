package com.terramaster.task;

import android.content.Context;

import com.terramaster.R;
import com.terramaster.dialog.ErrorToast;
import com.terramaster.dialog.SuccessToast;
import com.terramaster.ftp.FTPHelper;
import com.terramaster.model.FTPFileItem;

public class DeleteFTPFileTask extends WorkerThread {
    protected FTPFileItem fileItem;
    private OnSuccessListener listener;

    public DeleteFTPFileTask(Context context, FTPFileItem item, OnSuccessListener listener) {
        super(context);
        // TODO Auto-generated constructor stub
        enableProgressBar(true);
        this.listener = listener;
        this.fileItem = item;

    }

    @Override
    public String onWorkInBackground() {
        // TODO Auto-generated method stub
        try {
            FTPHelper ftpHelper = FTPHelper.getInstance();
            boolean isSuccess = ftpHelper.deleteFile(fileItem);
            if (isSuccess) {
                return "1";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected synchronized void onWorkFinished(String result) {
        // TODO Auto-generated method stub
        super.onWorkFinished(result);

        if (result != null) {
//	    DatabaseHelper dbHelper=DatabaseHelper.getInstance(mContext);
//	    TaskDetail taskDetail=new TaskDetail();
//	    taskDetail.setFrom(fileItem.getPath());
//	    Date date=new Date();
//	    taskDetail.setLastUpdated(date);
//	    taskDetail.setCreatedAt(date);
//	    taskDetail.setTaskType(DBKeys.TASK_DELETING_FILE);
//	    taskDetail.setTaskStatus(DBKeys.STATUS_COMPLETE);
//	    
//	    
//	    dbHelper.insertTask(taskDetail);
            new SuccessToast(mContext, mContext.getString(R.string.success_delete_file)).show();
            if (listener != null) {
                listener.onSuccess();
            }
        } else {
            new ErrorToast(mContext, mContext.getString(R.string.e_unknown)).show();
        }
    }

}
