package com.example.authregistr;



public class VideoModel {
    private String videoUrl;

    String name;



    public VideoModel(String videoUrl,String name) {

        this.videoUrl = videoUrl;
        this.name=name;
    }

    public String getName() {
        return name;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
