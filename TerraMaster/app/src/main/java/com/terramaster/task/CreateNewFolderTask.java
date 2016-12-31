package com.terramaster.task;

import android.content.Context;

import com.terramaster.R;
import com.terramaster.dialog.ErrorToast;
import com.terramaster.dialog.SuccessToast;
import com.terramaster.ftp.FTPHelper;
import com.terramaster.model.FTPFileItem;

public class CreateNewFolderTask extends WorkerThread {
    protected FTPFileItem fileItem;
    private OnSuccessListener listener;
    private String folderName;

    public CreateNewFolderTask(Context context, FTPFileItem item, String folderName, OnSuccessListener listener) {
        super(context);
        // TODO Auto-generated constructor stub
        enableProgressBar(true);
        this.listener = listener;
        this.fileItem = item;
        this.folderName = folderName;
    }

    @Override
    public String onWorkInBackground() {
        // TODO Auto-generated method stub
        try {
            FTPHelper ftpHelper = FTPHelper.getInstance();
            String remotePath;
            if (fileItem != null) {
                remotePath = fileItem.getPath() + "/" + folderName;
            } else {
                remotePath = folderName;
            }
            ftpHelper.createNewFolder(remotePath);
            return "1";

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
            new SuccessToast(mContext, mContext.getString(R.string.success_create_directory)).show();
            if (listener != null) {
                listener.onSuccess();
            }
        } else {
            new ErrorToast(mContext, mContext.getString(R.string.e_unknown)).show();
        }
    }

}
