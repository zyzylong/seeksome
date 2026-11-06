package com.zyzylong.seeksome.framework.common.error;

/**
 * 统一错误码契约，驱动响应码、提示信息与 HTTP 状态。
 */
public interface BaseErrorCode {

    String getCode();

    String getMessage();

    int getHttpStatus();
}
