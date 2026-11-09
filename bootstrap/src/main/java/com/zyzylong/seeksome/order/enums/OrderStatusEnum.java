package com.zyzylong.seeksome.order.enums;

import lombok.Getter;

/**
 * 订单状态。
 */
@Getter
public enum OrderStatusEnum {

    PENDING_PAYMENT("pending_payment"),
    PAID("paid"),
    DELIVERED("delivered"),
    COMPLETED("completed"),
    DEFAULTED("defaulted");

    private final String value;

    OrderStatusEnum(String value) {
        this.value = value;
    }
}
