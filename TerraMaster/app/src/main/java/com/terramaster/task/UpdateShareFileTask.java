package com.terramaster.task;

import android.content.Context;

import com.terramaster.ftp.FTPHelper;
import com.terramaster.utils.LogM;
import com.terramaster.utils.RandomString;
import com.terramaster.utils.SDCardUtils;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

public class UpdateShareFileTask extends WorkerThread {
    private final String SHARE_FILE_PATH = "User/admin/.data/share.php";
    private final String SHARE_FOLDER_PATH = "User/admin/.data";

    //For testing purpose...
//    private final String SHARE_FILE_PATH = "download/share.php";
//    private final String SHARE_FOLDER_PATH = "download";

    protected ArrayList<String> shareFileSIDList = new ArrayList<>();
    private ArrayList<String> shareFileList;

    public UpdateShareFileTask(Context context, ArrayList<String> shareFileList) {
        // TODO Auto-generated constructor stub
        super(context);
        this.shareFileList = shareFileList;
    }


    @Override
    public String onWorkInBackground() {
        // TODO Auto-generated method stub
        try {
            FTPHelper ftpHelper = FTPHelper.getInstance();

            File fileLocal = SDCardUtils.createCatcheFile(SHARE_FILE_PATH);

            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fileLocal));
            boolean success = ftpHelper.retrieveFile(SHARE_FILE_PATH, outputStream);
            outputStream.close();
            if (success) {
                // File is download in local...
            } else {
                if (fileLocal.isFile()) {
                    fileLocal.delete();
                }
                fileLocal.createNewFile();
            }

            // Read text from file

            final String PHP_TAG = "<php exit;??>";
            String text = readFile(fileLocal);
            JSONObject joMain = null;
            if (text != null && text.length() > 0) {
                text = text.replace(PHP_TAG, "");

                try {
                    joMain = new JSONObject(text);
                } catch (Exception e) {
                    //No need to maange
                    joMain = new JSONObject();
                }
            } else {
                joMain = new JSONObject();
            }

            LogM.e("text: " + text.toString());

            StringBuilder sb = new StringBuilder();
            sb.append(PHP_TAG);
            shareFileSIDList.clear();

            for (String shareFile : shareFileList) {
                String sid;
                do {
                    sid = new RandomString(8).nextString();
                    LogM.e("SID: " + sid);
                } while (shareFileSIDList.contains(sid));

                shareFileSIDList.add(sid);

                JSONObject joShare = new JSONObject();
                joShare.put("mtime", new Date().getTime());
                joShare.put("sid", sid);
                joShare.put("type", "file");
                joShare.put("path", shareFile);
                joShare.put("name", getNameFromPath(shareFile));
                joShare.put("time_to", "");
                joShare.put("share_password", "");
                joShare.put("code_read", "");
                joShare.put("not_download", "");
                joShare.put("num_view", 0);
                joShare.put("num_download", 0);

                joMain.put(sid, joShare);
            }

            sb.append(joMain.toString());

            FileOutputStream fOut = new FileOutputStream(fileLocal);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(sb.toString());
            myOutWriter.close();
            fOut.close();

            ftpHelper.uploadFileToFolder(fileLocal, SHARE_FOLDER_PATH);

            return "sucess";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getNameFromPath(String fromPath) {
        int index = fromPath.lastIndexOf("/");
        String name = null;
        if (index >= 0) {
            name = fromPath.substring(index);
        } else {
            name = fromPath;
        }
        return name;
    }

    private String readFile(File file) {
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            // You'll need to add proper error handling here
        }
        return text.toString();
    }

}
