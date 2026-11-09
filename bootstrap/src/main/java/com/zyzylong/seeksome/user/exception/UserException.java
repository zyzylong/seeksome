package com.zyzylong.seeksome.user.exception;

import com.zyzylong.seeksome.framework.error.BaseErrorCode;
import com.zyzylong.seeksome.framework.exception.AbstractException;

/**
 * 用户域业务异常。
 */
public class UserException extends AbstractException {

    public UserException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public UserException(BaseErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
