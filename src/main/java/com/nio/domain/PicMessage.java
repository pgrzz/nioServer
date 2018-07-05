package com.nio.domain;

public class PicMessage extends Message {

    private static final long serialVersionUID = -6849123470754667710L;
    private int Message_Type = 3;

    private String picName;

    private String picUrl;

    private byte[] picture;

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    @Override
    public String getId() {
        return getFrom();
    }
}
