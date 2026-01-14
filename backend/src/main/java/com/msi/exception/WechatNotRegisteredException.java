package com.msi.exception;

public class WechatNotRegisteredException extends RuntimeException {
    private String openid;

    public WechatNotRegisteredException(String message, String openid) {
        super(message);
        this.openid = openid;
    }

    public String getOpenid() {
        return openid;
    }
}
