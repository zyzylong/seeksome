package com.zyzylong.seeksome.auth.exception;

import com.zyzylong.seeksome.framework.error.BaseErrorCode;
import com.zyzylong.seeksome.framework.exception.AbstractException;

/**
 * 用户端认证异常。
 */
public class AppAuthException extends AbstractException {

    public AppAuthException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public AppAuthException(BaseErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
