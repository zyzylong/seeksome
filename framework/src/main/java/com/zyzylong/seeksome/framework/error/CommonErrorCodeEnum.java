package com.zyzylong.seeksome.framework.error;

/**
 * 通用错误码，覆盖成功与跨业务共享的失败场景。
 */
public enum CommonErrorCodeEnum implements BaseErrorCode {

    SUCCESS("00000", "成功", 200),
    PARAM_INVALID("A0400", "参数校验失败", 400),
    UNAUTHORIZED("A0401", "未登录或登录状态已失效", 401),
    NOT_FOUND("A0404", "请求资源不存在", 404),
    METHOD_NOT_ALLOWED("A0405", "请求方法不支持", 405),
    NOT_ACCEPTABLE("A0406", "无法生成客户端可接受的响应", 406),
    UNSUPPORTED_MEDIA_TYPE("A0415", "不支持的请求媒体类型", 415),
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
