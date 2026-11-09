package com.zyzylong.seeksome.auth.enums;

import com.zyzylong.seeksome.framework.error.BaseErrorCode;

/**
 * 用户端认证错误码。
 */
public enum AppAuthErrorCodeEnum implements BaseErrorCode {

    LOGIN_FAILED("A0205", "用户名或密码错误", 401),
    USERNAME_EXISTS("A0206", "用户名已存在", 409);

    private final String code;

    private final String message;

    private final int httpStatus;

    AppAuthErrorCodeEnum(String code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getHttpStatus() {
        return httpStatus;
    }
}
