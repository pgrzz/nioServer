package com.nio.domain;

public class Header implements java.io.Serializable {
    private static final long serialVersionUID = -6849123470754667710L;
    private int Len;
    private byte messageType;

    public int getLen() {
        return Len;
    }

    public void setLen(int len) {
        Len = len;
    }

    public byte getMessageType() {
        return messageType;
    }

    public void setMessageType(byte messageType) {
        this.messageType = messageType;
    }
}
