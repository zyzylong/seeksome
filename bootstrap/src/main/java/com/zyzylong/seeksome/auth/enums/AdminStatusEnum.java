package com.zyzylong.seeksome.auth.enums;

import lombok.Getter;

/**
 * 管理员账户状态。
 */
@Getter
public enum AdminStatusEnum {

    DISABLED(0),
    ENABLED(1);

    private final Integer value;

    AdminStatusEnum(Integer value) {
        this.value = value;
    }
}
