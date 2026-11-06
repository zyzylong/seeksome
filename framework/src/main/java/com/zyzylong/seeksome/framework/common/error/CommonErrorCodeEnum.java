package com.zyzylong.seeksome.framework.common.error;

/**
 * 通用错误码，覆盖成功与跨业务共享的失败场景。
 */
public enum CommonErrorCodeEnum implements BaseErrorCode {

    SUCCESS("00000", "成功", 200),
    PARAM_INVALID("A0400", "参数校验失败", 400),
    UNAUTHORIZED("A0401", "未登录或登录状态已失效", 401),
    SYSTEM_ERROR("B0500", "系统繁忙，请稍后重试", 500);

    private final String code;

    private final String message;

    private final int httpStatus;

    CommonErrorCodeEnum(String code, String message, int httpStatus) {
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
