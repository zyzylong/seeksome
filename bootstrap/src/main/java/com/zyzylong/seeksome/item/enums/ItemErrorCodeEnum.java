package com.zyzylong.seeksome.item.enums;

import com.zyzylong.seeksome.framework.error.BaseErrorCode;

/**
 * 拍品域错误码。
 */
public enum ItemErrorCodeEnum implements BaseErrorCode {

    CATEGORY_CODE_EXISTS("A0501", "分类编码已存在", 409),
    ITEM_STATUS_NOT_ALLOWED("A0502", "拍品当前状态不允许该操作", 409);

    private final String code;

    private final String message;

    private final int httpStatus;

    ItemErrorCodeEnum(String code, String message, int httpStatus) {
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
