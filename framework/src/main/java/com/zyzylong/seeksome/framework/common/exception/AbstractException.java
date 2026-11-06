package com.zyzylong.seeksome.framework.common.exception;

import com.zyzylong.seeksome.framework.common.error.BaseErrorCode;
import lombok.Getter;

/**
 * 业务异常基类，携带统一错误码供全局异常处理映射。
 */
@Getter
public class AbstractException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public AbstractException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AbstractException(BaseErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
