package com.jc.retrofit.wiki.bean;

/**
 * Created by 江俊超 on 2019/2/11.
 * Version:1.0
 * Description:
 * ChangeLog:
 */
public class BaseDataModel<T> {
    private int code;
    private T data;
    private String msg;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccessful() {
        return code == 200;
    }

}
