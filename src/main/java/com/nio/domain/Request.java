package com.nio.domain;

public class Request implements java.io.Serializable {
    private static final long serialVersionUID = -6849123470754667710L;

    private Header header;
    private byte[] body;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
