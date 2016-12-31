package com.terramaster.model;

import org.apache.commons.net.ftp.FTPFile;

import java.io.Serializable;
import java.util.Date;

public class TaskDetail implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private long rowId;
    private String from, to, macAddress;
    private int taskType, taskStatus;
    private long taskProcess = 0;
    private long taskFileSize = 0;
    private Date lastUpdated, createdAt;

    public long getRowId() {
        return rowId;
    }

    public void setRowId(long rowId) {
        this.rowId = rowId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public long getProcess(){
        return taskProcess;
    }

    public void setProcess(long process){
        this.taskProcess = process;
    }

    public long getTaskFileSize(){
        return taskFileSize;
    }

    public void setTaskFileSize(long size){
        this.taskFileSize = size;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public int getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(int taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }


}
