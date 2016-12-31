package com.terramaster.task;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.terramaster.R;
import com.terramaster.dialog.ErrorToast;
import com.terramaster.ftp.FTPHelper;
import com.terramaster.ftp.TerCls;
import com.terramaster.model.FTPFileItem;
import com.terramaster.utils.LogM;
import com.terramaster.utils.SDCardUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CacheFTPFileTask extends WorkerThread {
    protected FTPFileItem fileItem;
    private OnCacheFileListener listener;
    private File file;
    private FTPHelper ftpHelper;
    private OutputStream outputStream = null;

    public CacheFTPFileTask(Context context, FTPFileItem item, OnCacheFileListener listener) {
        super(context);
        // TODO Auto-generated constructor stub
        enableProgressBar(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(true);
            }
        });
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        setName("cache thread");
        this.listener = listener;
        this.fileItem = item;

    }

    @Override
    public void cancel(boolean b) {
        super.cancel(b);
        Log.i("cancle",getName());
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if(ftpHelper!=null){
//                    ftpHelper.abortConnection();
//                }
//            }
//        }).start();
    }

    @Override
    public String onWorkInBackground() {
        // TODO Auto-generated method stub
        try {
            ftpHelper = FTPHelper.getInstance();
            file = SDCardUtils.createCatcheFile(fileItem.getPath());
            LogM.e("Catch File name: " + file.getAbsolutePath());
            if(outputStream == null)
                outputStream = new BufferedOutputStream(new FileOutputStream(file));
            boolean success = ftpHelper.retrieveFile(fileItem.getPath(), outputStream);
            outputStream.close();
            if (success) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected synchronized void onWorkCancelled(String result) {
        super.onWorkCancelled(result);
        LogM.i("CacheFTPFileTask: onWorkCancelled");
    }

    @Override
    protected synchronized void onWorkFinished(String result) {
        // TODO Auto-generated method stub
        super.onWorkFinished(result);
        LogM.e("Catch finished andsontan");
        if (result != null) {
            if (listener != null) {
                listener.onSuccess(file);
            }
        } else {
            new ErrorToast(mContext, mContext.getString(R.string.e_unknown)).show();
        }
    }

}
