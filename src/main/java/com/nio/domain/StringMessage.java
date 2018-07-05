package com.nio.domain;


public class StringMessage extends Message {
    private static final long serialVersionUID = -6849123470754667710L;

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getId() {
        return getFrom();
    }
}
