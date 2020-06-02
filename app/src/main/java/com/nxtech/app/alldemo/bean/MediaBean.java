package com.nxtech.app.alldemo.bean;

public class MediaBean {

    private String name;
    private String path;
    private long size;
    private int duration;

    public MediaBean(String n, String p, long s, int dur){
        this.path = p;
        this.size = s;
        this.name = n;
        this.duration = dur;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public int getDuration() {
        return duration;
    }
}
