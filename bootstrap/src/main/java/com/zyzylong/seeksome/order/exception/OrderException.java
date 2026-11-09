package com.zyzylong.seeksome.order.exception;

import com.zyzylong.seeksome.framework.error.BaseErrorCode;
import com.zyzylong.seeksome.framework.exception.AbstractException;

/**
 * 订单域业务异常。
 */
public class OrderException extends AbstractException {

    public OrderException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public OrderException(BaseErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
