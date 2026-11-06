package com.zyzylong.seeksome.admin.exception;

import com.zyzylong.seeksome.framework.common.error.BaseErrorCode;
import com.zyzylong.seeksome.framework.common.exception.AbstractException;

/**
 * 管理员认证异常。
 */
public class AdminAuthException extends AbstractException {

    public AdminAuthException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public AdminAuthException(BaseErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
