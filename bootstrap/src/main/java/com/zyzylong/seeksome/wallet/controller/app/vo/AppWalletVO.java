package com.zyzylong.seeksome.wallet.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 钱包概览。
 */
@Schema(description = "钱包概览")
@Data
@Builder
public class AppWalletVO {

    @Schema(description = "钱包可用余额（元）", example = "120.50")
    private BigDecimal walletBalance;

    @Schema(description = "冻结金额（元）", example = "10.00")
    private BigDecimal frozenBalance;
}
