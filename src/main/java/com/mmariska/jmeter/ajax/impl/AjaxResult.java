package com.mmariska.jmeter.ajax.impl;


public class AjaxResult {

    public static final int RESULT_OK = 200;
    public static final int ERROR_RESULT = -1;
    private long elapsedTimeMs;
    private int result;
    private String url;
    private int responseByteSize;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getResult() {
        return result;
    }

    protected void setResult(int result) {
        this.result = result;
    }

    public boolean isOk() {
        return getResult() == RESULT_OK;
    }

    public long getElapsedTime() {
        return elapsedTimeMs;
    }

    protected void finish(long start) {
        this.elapsedTimeMs = (System.nanoTime() - start) / 1000000;
    }

    public int getResponseByteSize() {
        return responseByteSize;
    }

    void setResponseByteSize(int byteSize) {
        this.responseByteSize = byteSize;
    }
}
