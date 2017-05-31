package com.nownews.mobile.Dao.Entity;

/**
 * Created by ChengYuanChin on 2017/4/24.
 */

public class ResponseErr {
    private int status_code;
    private String message;
    private int EpgId;

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
