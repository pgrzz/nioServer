package com.nio.domain;

public class AmrMessage extends Message {
    private static final long serialVersionUID = -6849123470754667710L;

    private int recorderLength;

    private byte[] arm;

    public int getRecorderLength() {
        return recorderLength;
    }

    public void setRecorderLength(int recorderLength) {
        this.recorderLength = recorderLength;
    }

    public byte[] getArm() {
        return arm;
    }

    public void setArm(byte[] arm) {
        this.arm = arm;
    }

    @Override
    public String getId() {
        return super.getFrom();
    }
}
