package com.zyzylong.seeksome.admin.domain.enums;

import com.zyzylong.seeksome.framework.common.error.BaseErrorCode;

/**
 * 管理员认证错误码。
 */
public enum AdminAuthErrorCodeEnum implements BaseErrorCode {

    LOGIN_FAILED("A0201", "用户名或密码错误", 401),
    ACCOUNT_LOCKED("A0202", "账号已锁定，请15分钟后重试", 423),
    ACCOUNT_DISABLED("A0203", "账号已被禁用", 403),
    IP_RATE_LIMITED("A0204", "请求过于频繁，请稍后再试", 429);

    private final String code;

    private final String message;

    private final int httpStatus;

    AdminAuthErrorCodeEnum(String code, String message, int httpStatus) {
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
