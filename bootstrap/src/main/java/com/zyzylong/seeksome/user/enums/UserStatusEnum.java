package com.zyzylong.seeksome.user.enums;

import lombok.Getter;

/**
 * 用户账户状态。
 */
@Getter
public enum UserStatusEnum {

    NORMAL("normal"),
    FROZEN("frozen");

    private final String value;

    UserStatusEnum(String value) {
        this.value = value;
    }
}
