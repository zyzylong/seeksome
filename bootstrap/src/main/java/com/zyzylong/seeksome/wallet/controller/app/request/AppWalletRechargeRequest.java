package com.zyzylong.seeksome.wallet.controller.app.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 钱包充值请求。
 */
@Schema(description = "钱包充值请求")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppWalletRechargeRequest {

    @Schema(description = "充值金额，0.01 ~ 10000.00 元，最多两位小数", example = "100.00")
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额不能小于 0.01 元")
    @DecimalMax(value = "10000.00", message = "单笔充值金额不能超过 10000 元")
    @Digits(integer = 8, fraction = 2, message = "充值金额最多保留两位小数")
    private BigDecimal amount;
}
