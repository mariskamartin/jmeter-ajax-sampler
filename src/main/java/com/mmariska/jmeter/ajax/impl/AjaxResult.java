package com.mmariska.jmeter.ajax.impl;

import java.util.Date;


public class AjaxResult {

    public static final int RESULT_OK = 200;
    public static final int ERROR_RESULT = -1;
    protected long elapsedTime;
    private int result;
    private String url;

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
        return elapsedTime;
    }

    protected void finish(Date start) {
        this.elapsedTime = (new Date()).getTime() - start.getTime();
    }
}
