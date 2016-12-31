package com.terramaster.model;

import java.io.Serializable;
import java.util.Date;

public class AlbumBackupDetail implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private long rowId;
    private String from, to, macAddress;
    private int bakupMode, noOfBackupPhotos;
    private Date lastUpdated, createdAt;
    private boolean isEnabled;

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

    public void setTo(String to) {
        this.to = to;
    }

    public int getBakupMode() {
        return bakupMode;
    }

    public void setBakupMode(int bakupMode) {
        this.bakupMode = bakupMode;
    }

    public int getNoOfBackupPhotos() {
        return noOfBackupPhotos;
    }

    public void setNoOfBackupPhotos(int noOfBackupPhotos) {
        this.noOfBackupPhotos = noOfBackupPhotos;
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

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }


}
