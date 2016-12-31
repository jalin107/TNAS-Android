package com.terramaster.model;

import com.terramaster.utils.StringUtils;

import java.io.File;
import java.util.Date;

public class FileItem implements Comparable<FileItem> {
    private File file;


    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return file.getName();
    }

    public Date getDate() {
        return new Date(file.lastModified());
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public String getFileSize() {
        return StringUtils.formatFileSize(file.length());
    }

    public int compareTo(FileItem another) {
        int c = ((Boolean) !isDirectory()).compareTo((Boolean) !another.isDirectory());
        if (c == 0) {
            c = getName().compareTo(another.getName());
        }
        return c;
    }

}