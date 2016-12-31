package com.terramaster.model;

import java.io.File;
import java.util.ArrayList;

public class AlbumDetail {
    public String path;
    public int noOfPhotos;
    public ArrayList<String> images = new ArrayList<>();
    public AlbumDetail(String path, String imagePath) {
        // TODO Auto-generated constructor stub
        this.path = path;
        this.noOfPhotos = 1;
        images.add(imagePath);
    }

    public String getAlbumName() {
        return new File(path).getName();
    }
}
