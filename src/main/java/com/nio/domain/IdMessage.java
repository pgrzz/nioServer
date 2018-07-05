package com.nio.domain;

public class IdMessage extends Message {
    private static final long serialVersionUID = -6849123470754667710L;

    @Override
    public String getId() {
        return getFrom();
    }
}
