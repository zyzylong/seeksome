package com.zyzylong.seeksome.framework.common.result;

import com.zyzylong.seeksome.framework.common.error.BaseErrorCode;
import com.zyzylong.seeksome.framework.common.error.CommonErrorCodeEnum;
import lombok.Data;
import org.slf4j.MDC;

/**
 * 统一响应结构，携带链路请求标识。
 */
@Data
public class Result<T> {

    private String code;

    private String message;

    private T data;

    private String requestId;

    private Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = MDC.get("requestId");
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(CommonErrorCodeEnum.SUCCESS.getCode(), CommonErrorCodeEnum.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> failure(BaseErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> Result<T> failure(BaseErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }
}
