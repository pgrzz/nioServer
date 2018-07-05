package com.nio.domain;

public abstract class Message implements java.io.Serializable {
    private static final long serialVersionUID = -6849123470754667710L;

    private long createdTime;
    private String from;
    private String to;


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

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public abstract String getId();

}
