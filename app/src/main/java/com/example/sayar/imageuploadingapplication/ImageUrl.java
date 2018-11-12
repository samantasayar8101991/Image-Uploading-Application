package com.example.sayar.imageuploadingapplication;

import java.io.Serializable;

public class ImageUrl implements Serializable {
    public String downloadUrl;
    public String name;


    public ImageUrl(String downloadUrl, String name) {
        this.downloadUrl = downloadUrl;
        this.name = name;
    }
    public ImageUrl(){

    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public ImageUrl(String dUrl) {
        this.downloadUrl = dUrl;
    }
}
