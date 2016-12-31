package com.terramaster.ftp;

import android.util.Log;

import com.terramaster.dbhelper.DBKeys;
import com.terramaster.dbhelper.DatabaseHelper;
import com.terramaster.model.FTPFileItem;
import com.terramaster.model.TaskDetail;
import com.terramaster.service.BackgroundTaskService;
import com.terramaster.utils.LogM;
import com.terramaster.utils.StringUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.aflx.sardine.Sardine;
import de.aflx.sardine.SardineFactory;

public class FTPHelper {
    private static final String TAG = "FTPHelper";
    public boolean ftp_mod=true;
    private static FTPHelper instance;
    private final String TEMP_FILE_PREFIX = "tmpmk9jk-";
    private FTPClient mFtpClient = new FTPClient();
    public  Sardine sardine = null;
    private boolean isLogin;
    // andsontan
    public String xip = null;
    private int xport= 21;
    public String xuserName;
    public   String xpass;
    //andsontan
    public static FTPHelper getInstance() {
        if (instance == null) {
            instance = new FTPHelper();//
        }
        return instance;
    }

    public boolean isLogin() {
        return isLogin;
    }
    public void check_sssion()
    {
        try {
            mFtpClient.sendNoOp();
        }catch (Exception e)
        {
            logIn();
        }
    }
    public void logIn() {
         try{
             mFtpClient.disconnect();
             FTPClientConfig ftpClientCfg = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
             ftpClientCfg.setLenientFutureDates(true);
             mFtpClient.configure(ftpClientCfg);
             mFtpClient.setAutodetectUTF8(true);
             mFtpClient.setControlEncoding("UTF8");
             mFtpClient.setConnectTimeout(60 * 1000);
             mFtpClient.setDataTimeout(10 * 1000);
             mFtpClient.connect(InetAddress.getByName(xip), xport);
             isLogin = mFtpClient.login(xuserName, xpass);
             if (FTPReply.isPositiveCompletion(mFtpClient.getReplyCode())) {
                 mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
                 mFtpClient.enterLocalPassiveMode();
             }
             Log.i(TAG, "isFTPConnected " + String.valueOf(isLogin));
        }catch (Exception e){
             Log.e("ftphelp","login faile.....");
        }
    }
    public TerCls setCopyStream()
    {
        TerCls tcls = new TerCls();
        mFtpClient.setCopyStreamListener(tcls);
        return tcls;
    }

    public void logIn(String ip, int port, String userName, String pass) throws Exception {
        xport= port;
        xuserName= userName;
        xpass=pass;
        xip=ip;
        FTPClientConfig ftpClientCfg = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
        ftpClientCfg.setLenientFutureDates(true);
        mFtpClient.configure(ftpClientCfg);
        mFtpClient.setAutodetectUTF8(true);
        mFtpClient.setControlEncoding("UTF8");
        mFtpClient.setConnectTimeout(60 * 1000);
        mFtpClient.setDataTimeout(10 * 1000);
        mFtpClient.connect(InetAddress.getByName(ip), port);
        isLogin = mFtpClient.login(userName, pass);
        if (FTPReply.isPositiveCompletion(mFtpClient.getReplyCode())) {
            mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
            mFtpClient.enterLocalPassiveMode();
        }
        Log.i(TAG, "isFTPConnected " + String.valueOf(isLogin));
    }

    public void logOut() throws Exception {
        isLogin = !mFtpClient.logout();
        Log.i(TAG, "isFTPConnected " + String.valueOf(isLogin));
    }

    public List<FTPFileItem> fetchAllFiles(FTPFileItem ftpFileItem, String deviceName) throws Exception {
        List<FTPFileItem> list = new ArrayList<FTPFileItem>();
        check_sssion();
        FTPFile[] mFileArray = null;
        if (ftpFileItem == null) {
            mFileArray = mFtpClient.listFiles();
        } else {
            Log.i(TAG,"Ftp File path: " + ftpFileItem.getPath());
            mFileArray = mFtpClient.listFiles(ftpFileItem.getPath());
        }
        LogM.i("mFileArray: " + mFileArray.length);

        for (FTPFile ftpFile : mFileArray) {
            if (ftpFile.getName().startsWith(".")) {
                continue;
            } else if (ftpFile.getName().startsWith(TEMP_FILE_PREFIX)) {
                continue;
            }
            FTPFileItem item = new FTPFileItem();
            item.setFtpFile(ftpFile);
            if (ftpFileItem == null) {
                item.setPath("");
            } else {
                item.setPath(ftpFileItem.getPath());
            }
            list.add(item);
        }

        Collections.sort(list);
        return list;
    }

    public FTPClient getmFtpClient(){
        check_sssion();
        return mFtpClient;
    }

    public int download(TaskDetail taskDetail, DatabaseHelper dbHelper) throws IOException{
        //设置被动模式
        mFtpClient.enterLocalPassiveMode();
        //设置以二进制方式传输
        mFtpClient.setFileType(FTP.BINARY_FILE_TYPE);
        int result;
        long lRemoteSize = taskDetail.getTaskFileSize();
        File localfile = new File(taskDetail.getTo());
        String remotefile = taskDetail.getFrom();
        if(localfile.exists()){
            long localSize = localfile.length();
            //判断本地文件大小是否大于远程文件大小
            if(localSize >= lRemoteSize){
                System.out.println("本地文件大于远程文件，下载中止");
                return 2;
            }
            //进行断点续传，并记录状态
            FileOutputStream out = new FileOutputStream(localfile,true);
            mFtpClient.setRestartOffset(localSize);
            InputStream in = mFtpClient.retrieveFileStream(remotefile);
            byte[] bytes = new byte[1024];
            long step = lRemoteSize /100;
            long process = localSize /step;
            int c;
            while((c = in.read(bytes))!= -1){
                out.write(bytes,0,c);
                localSize += c;
                long nowProcess = localSize /step;
                if(nowProcess > process){
                    process = nowProcess;
                    taskDetail.setProcess(process);
                    dbHelper.updateTaskDetail(taskDetail);
                }
            }
            in.close();
            out.close();
            boolean isDo = mFtpClient.completePendingCommand();
            if(isDo){
                result = 1;
            }else {
                result = 0;
            }
        }else {
            OutputStream out = new FileOutputStream(localfile);
            InputStream in= mFtpClient.retrieveFileStream(remotefile);
            byte[] bytes = new byte[1024];
            long step = lRemoteSize /100;
            long process=0;
            long localSize = 0L;
            int c;
            while((c = in.read(bytes))!= -1){
                out.write(bytes, 0, c);
                localSize+=c;
                long nowProcess = localSize /step;
                if(nowProcess > process){
                    process = nowProcess;
                    taskDetail.setProcess(process);
                    dbHelper.updateTaskDetail(taskDetail);
                }
            }
            in.close();
            out.close();
            boolean upNewStatus = mFtpClient.completePendingCommand();
            if(upNewStatus){
                result = 1;
            }else {
                result = 0;
            }
        }
        return result;
    }

    public boolean deleteFile(FTPFileItem file) throws Exception {
        check_sssion();
        return mFtpClient.deleteFile(file.getPath());
    }

    public boolean deleteFile(String remotePath) throws Exception {
        check_sssion();
        return mFtpClient.deleteFile(remotePath);
    }

    public boolean removeDirectory(FTPFileItem file) throws Exception {
        check_sssion();
        return removeDirectory(file.getPath(), "");
    }

    /**
     * Removes a non-empty directory by delete all its sub files and sub
     * directories recursively. And finally remove the directory.
     */
    private boolean removeDirectory(String parentDir, String currentDir) throws Exception {
        String dirToList = parentDir;
        if (!currentDir.equals("")) dirToList += "/" + currentDir;
        FTPFile[] subFiles = mFtpClient.listFiles(dirToList);
        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = dirToList + "/" + currentFileName;
                if (aFile.isDirectory()) {
                    removeDirectory(dirToList, currentFileName);
                }else{
                    LogM.e("mFtpClient.deleteFile:"+filePath);
                    boolean deleted = mFtpClient.deleteFile(filePath);
                    LogM.e("mFtpClient.deleteFile:"+deleted);
                }
            }
        }
        // finally, remove the directory itself
        boolean removed = mFtpClient.removeDirectory(dirToList);
        LogM.e("mFtpClient.removeDirectory:"+removed);
        return removed;
    }

    public boolean renameFile(String from, String to) throws Exception  {
        check_sssion();
        boolean ret =  mFtpClient.rename(from, to);
        return ret;
    }
    public Socket get_socket()
    {
        return mFtpClient.get_socket();
    }
    public boolean uploadFileToFolder(File file, String folderPath) throws Exception {
        boolean success = false;
        check_sssion();
        if (file.exists()) {
            InputStream inputStream = new FileInputStream(file);
            String fileName = TEMP_FILE_PREFIX + file.getName();
            String remoteFile = !StringUtils.isEmpty(folderPath) ? folderPath + "/" + fileName : fileName;
            LogM.e("add File " + remoteFile);
            success = mFtpClient.storeFile(remoteFile, inputStream);

            inputStream.close();
            if (success) {
                success = renameFile(remoteFile, remoteFile.replace(TEMP_FILE_PREFIX, ""));
            }
        }
        return success;
    }

    /***
     * @上传文件夹
     * @param localDirectory 当地文件夹
     * @param remoteDirectoryPath Ftp 服务器路径 以目录"/"结束
     * */
    public boolean uploadDirectory(String localDirectory, String remoteDirectoryPath) throws Exception {
        File src = new File(localDirectory);
        remoteDirectoryPath = remoteDirectoryPath + "/" + src.getName();
        mFtpClient.makeDirectory(remoteDirectoryPath);
        File[] allFile = src.listFiles();
        for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
            if (!allFile[currentFile].isDirectory()) {
                String srcName = allFile[currentFile].getPath().toString();
                uploadFileToFolder(new File(srcName), remoteDirectoryPath);
            }
        }
        for (int currentFile = 0; currentFile < allFile.length; currentFile++) {
            if (allFile[currentFile].isDirectory()) {
                uploadDirectory(allFile[currentFile].getPath().toString(), remoteDirectoryPath);
            }
        }
        return true;
    }

    public void createNewFolder(String remotePath) throws Exception {
        check_sssion();
        mFtpClient.makeDirectory(remotePath);
    }

    public boolean retrieveFile(String remote, OutputStream outputStream) throws Exception {
        check_sssion();
        return mFtpClient.retrieveFile(remote, outputStream);
    }

    public boolean setRestartOffset(long localSize){
        mFtpClient.setRestartOffset(localSize);
        return true;
    }

    public boolean checkFileExists(String filePath) throws Exception {
        check_sssion();
        InputStream inputStream = mFtpClient.retrieveFileStream(filePath);
        int returnCode = mFtpClient.getReplyCode();
        if (inputStream == null || returnCode == 550) {
            return false;
        }
        return true;
    }

    public void abortConnection(){
        if (mFtpClient!=null && mFtpClient.isConnected()) {
            try {
                if (mFtpClient.abort()) {
                    Log.i("stop ok...","hahh ");
                }else{
                    Log.i("stop fail...","kkkk ");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("no  connnect...","aaaaaaaaaaaa ");
            // Not connected!
        }
    }

    public void reinit() {
        try {
            mFtpClient.reinitialize();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
