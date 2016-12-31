package com.terramaster.samba;
import android.util.Log;

import com.terramaster.model.FTPFileItem;
import com.terramaster.utils.LogM;
import com.terramaster.utils.StringUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jcifs.smb.*;

public class  SambaHelper {
    private static final String TAG = "SambaHelper";
    public boolean ftp_mod=true;
    private static com.terramaster.samba.SambaHelper instance;
    private final String TEMP_FILE_PREFIX = "tmpmk9jk-";
    private FTPClient mFtpClient =  new FTPClient();
    private boolean isLogin;

    public static com.terramaster.samba.SambaHelper getInstance() {
        if (instance == null) {
            instance = new com.terramaster.samba.SambaHelper();
        }
        return instance;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void logIn(String ip, int port, String userName, String pass) throws Exception {

        mFtpClient.setConnectTimeout(10 * 1000);
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

        FTPFile[] mFileArray = null;
        if (ftpFileItem == null) {
            mFileArray = mFtpClient.listFiles();
        } else {
            LogM.i("Ftp File path: " + ftpFileItem.getPath());
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

    public boolean deleteFile(FTPFileItem file) throws IOException {
        return mFtpClient.deleteFile(file.getPath());
    }

    public boolean deleteFile(String remotePath) throws IOException {
        return mFtpClient.deleteFile(remotePath);
    }

    public boolean removeDirectory(FTPFileItem file) throws IOException {
        return removeDirectory(file.getPath(), "");
    }

    /**
     * Removes a non-empty directory by delete all its sub files and sub
     * directories recursively. And finally remove the directory.
     */
    private boolean removeDirectory(String parentDir, String currentDir) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }

        FTPFile[] subFiles = mFtpClient.listFiles(dirToList);

        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) {
                    // skip parent directory and the directory itself
                    continue;
                }
                String filePath = parentDir + "/" + currentDir + "/" + currentFileName;
                if (currentDir.equals("")) {
                    filePath = parentDir + "/" + currentFileName;
                }

                if (aFile.isDirectory()) {
                    // remove the sub directory
                    removeDirectory(dirToList, currentFileName);
                } else {
                    // delete the file
                    boolean deleted = mFtpClient.deleteFile(filePath);
//		    if (deleted)
//		    {
//			LogM.e("DELETED the file: " + filePath);
//		    }
//		    else
//		    {
//			LogM.e("CANNOT delete the file: " + filePath);
//		    }
                }
            }

        }
        // finally, remove the directory itself
        boolean removed = mFtpClient.removeDirectory(dirToList);
//	if (removed)
//	{
//	    LogM.e("REMOVED the directory: " + dirToList);
//	}
//	else
//	{
//	    LogM.e("CANNOT remove the directory: " + dirToList);
//	}
        return removed;
    }

    public boolean renameFile(String from, String to) throws IOException {
        return mFtpClient.rename(from, to);
    }

    public boolean uploadFileToFolder(File file, String folderPath) throws IOException {
        boolean success = false;
        if (file.exists()) {
            InputStream inputStream = new FileInputStream(file);
            String fileName = TEMP_FILE_PREFIX + file.getName();
            String remoteFile = !StringUtils.isEmpty(folderPath) ? folderPath + "/" + fileName : fileName;
            LogM.e("add File " + remoteFile);
            // OutputStream outputStream=mFtpClient.storeFileStream(remoteFile);
            // byte[] bytesIn = new byte[4096];
            // int read = 0;
            //
            // while ((read = inputStream.read(bytesIn)) != -1) {
            // outputStream.write(bytesIn, 0, read);
            // }
            // inputStream.close();
            // outputStream.close();

            success = mFtpClient.storeFile(remoteFile, inputStream);
            // success=mFtpClient.completePendingCommand();
            inputStream.close();
            if (success) {
                success = renameFile(remoteFile, remoteFile.replace(TEMP_FILE_PREFIX, ""));
            }
        }

        // mFtpClient.storeFileStream("Upload.doc");
        return success;
    }


    public void createNewFolder(String remotePath) throws IOException {
        mFtpClient.makeDirectory(remotePath);
    }

    public boolean retrieveFile(String remote, OutputStream outputStream) throws IOException {
        return mFtpClient.retrieveFile(remote, outputStream);
    }

    public boolean checkFileExists(String filePath) throws IOException {
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
                    // Connection aborted!
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Not connected!
        }
    }
}
