package com.zyzylong.seeksome.item.enums;

import lombok.Getter;

/**
 * 拍品状态。
 */
@Getter
public enum ItemStatusEnum {

    PENDING_REVIEW("pending_review"),
    ACTIVE("active"),
    ENDED("ended"),
    REJECTED("rejected"),
    CANCELLED("cancelled");

    private final String value;

    ItemStatusEnum(String value) {
        this.value = value;
    }
}
