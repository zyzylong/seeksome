package com.zyzylong.seeksome.wallet.exception;

import com.zyzylong.seeksome.framework.error.BaseErrorCode;
import com.zyzylong.seeksome.framework.exception.AbstractException;

/**
 * 钱包域业务异常。
 */
public class WalletException extends AbstractException {

    public WalletException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public WalletException(BaseErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
