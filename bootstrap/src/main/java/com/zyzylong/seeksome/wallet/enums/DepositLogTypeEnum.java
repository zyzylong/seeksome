package com.zyzylong.seeksome.wallet.enums;

import lombok.Getter;

/**
 * 保证金流水类型。
 */
@Getter
public enum DepositLogTypeEnum {

    FREEZE("freeze"),
    UNFREEZE("unfreeze"),
    DEDUCT("deduct");

    private final String value;

    DepositLogTypeEnum(String value) {
        this.value = value;
    }
}
