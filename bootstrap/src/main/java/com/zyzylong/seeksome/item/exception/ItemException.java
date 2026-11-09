package com.zyzylong.seeksome.item.exception;

import com.zyzylong.seeksome.framework.error.BaseErrorCode;
import com.zyzylong.seeksome.framework.exception.AbstractException;

/**
 * 拍品域业务异常。
 */
public class ItemException extends AbstractException {

    public ItemException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public ItemException(BaseErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
