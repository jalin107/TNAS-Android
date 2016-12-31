package com.terramaster.model;

import com.terramaster.utils.StringUtils;

import org.apache.commons.net.ftp.FTPFile;

import java.io.Serializable;
import java.util.Date;

public class FTPFileItem implements Comparable<FTPFileItem>, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private FTPFile ftpFile;

    private String path;

    public FTPFile getFtpFile() {
        return ftpFile;
    }

    public void setFtpFile(FTPFile ftpFile) {
        this.ftpFile = ftpFile;
    }

    public String getNewPath(String name) {
        if (StringUtils.isEmpty(path)) {
            return name;
        }
        return path + "/" + name;
    }

    public String getPath() {
        if (StringUtils.isEmpty(path)) {
            return getName();
        }
        return path + "/" + getName();
    }

    public String getAbsolutePath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isWritable() {
        return ftpFile.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION);
    }

    public String getName() {
        // TODO Auto-generated method stub
        return ftpFile.getName();
    }

    public void setName(String name) {
        ftpFile.setName(name);
    }

    public boolean isDirectory() {
        // TODO Auto-generated method stub
        return ftpFile.isDirectory();
    }

    public Date getDate() {
        // TODO Auto-generated method stub
        return ftpFile.getTimestamp().getTime();
    }

    public String getFileSize() {
        // TODO Auto-generated method stub
        return StringUtils.formatFileSize(ftpFile.getSize());
    }

    public long getSize(){
        return ftpFile.getSize();
    }

    @Override
    public int compareTo(FTPFileItem another) {
        // TODO Auto-generated method stub
        int c = ((Boolean) !isDirectory()).compareTo((Boolean) !another.isDirectory());
        if (c == 0) {
            c = getName().compareToIgnoreCase(another.getName());
        }
        return c;
    }
}
