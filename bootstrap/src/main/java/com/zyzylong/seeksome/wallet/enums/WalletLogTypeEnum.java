package com.zyzylong.seeksome.wallet.enums;

import lombok.Getter;

/**
 * 钱包流水类型。
 */
@Getter
public enum WalletLogTypeEnum {

    RECHARGE("recharge"),
    FREEZE("freeze"),
    UNFREEZE("unfreeze"),
    DEDUCT("deduct"),
    PAY("pay"),
    INCOME("income");

    private final String value;

    WalletLogTypeEnum(String value) {
        this.value = value;
    }
}
