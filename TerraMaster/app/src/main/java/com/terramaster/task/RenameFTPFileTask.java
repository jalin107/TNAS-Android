package com.terramaster.task;

import android.content.Context;

import com.terramaster.R;
import com.terramaster.dialog.ErrorToast;
import com.terramaster.dialog.SuccessToast;
import com.terramaster.ftp.FTPHelper;
import com.terramaster.model.FTPFileItem;
import com.terramaster.utils.LogM;
import java.io.*;
import java.net.Socket;

public class RenameFTPFileTask extends WorkerThread {
    protected FTPFileItem fileItem;
    private OnSuccessListener listener;
    private String newName;

    public RenameFTPFileTask(Context context, FTPFileItem item, String newName, OnSuccessListener listener) {
        super(context);
        // TODO Auto-generated constructor stub
        enableProgressBar(true);
        this.listener = listener;
        this.fileItem = item;
        this.newName = newName;
    }

    @Override
    public String onWorkInBackground() {
        // TODO Auto-generated method stub
        FTPHelper ftpHelper = FTPHelper.getInstance();
        try {

            boolean isSuccess = ftpHelper.renameFile(fileItem.getPath(), fileItem.getNewPath(newName));
            LogM.e("Rename tgm" + isSuccess);
            if (isSuccess) {
                return "1";

            }
        } catch (Exception e) {
            e.printStackTrace();
            if(e.toString().indexOf("closed")>0)
            {
                try {
                    //isAvailable
                 //   LogM.e("socket " + ftpHelper.isAvailable());
                 //   Socket s = ftpHelper.get_socket();
                   // Reader reader = new InputStreamReader(s.getInputStream());
                  //  char [] buffer = new char[1024];
                  //  reader.read(buffer);
                  //  LogM.e("rename e " +buffer.toString());
                    ftpHelper.logIn();


                }catch (Exception e1)
                {

                }
               /// ftpHelper.
            }
        }
        return null;
    }

    @Override
    protected synchronized void onWorkFinished(String result) {
        // TODO Auto-generated method stub
        super.onWorkFinished(result);

        if (result != null) {
            fileItem.setName(newName);

//	    DatabaseHelper dbHelper=DatabaseHelper.getInstance(mContext);
//	    TaskDetail taskDetail=new TaskDetail();
//	    taskDetail.setFrom(fileItem.getPath());
//	    
//	    taskDetail.setTo(fileItem.getPath());
//	    Date date=new Date();
//	    taskDetail.setLastUpdated(date);
//	    taskDetail.setCreatedAt(date);
//	    taskDetail.setTaskType(DBKeys.TASK_RENAMEING);
//	    taskDetail.setTaskStatus(DBKeys.STATUS_COMPLETE);
//	    
//	    dbHelper.insertTask(taskDetail);
            new SuccessToast(mContext, mContext.getString(fileItem.isDirectory() ? R.string.success_rename_directory : R.string.success_rename_file)).show();
            if (listener != null) {
                listener.onSuccess();
            }
        } else {
            new ErrorToast(mContext, mContext.getString(R.string.e_unknown)).show();
        }
    }

}
