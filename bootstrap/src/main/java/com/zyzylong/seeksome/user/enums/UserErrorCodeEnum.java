package com.zyzylong.seeksome.user.enums;

import com.zyzylong.seeksome.framework.error.BaseErrorCode;

/**
 * 用户域错误码。
 */
public enum UserErrorCodeEnum implements BaseErrorCode {

    USER_FROZEN("A0301", "账号已被冻结", 403);

    private final String code;

    private final String message;

    private final int httpStatus;

    UserErrorCodeEnum(String code, String message, int httpStatus) {
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
