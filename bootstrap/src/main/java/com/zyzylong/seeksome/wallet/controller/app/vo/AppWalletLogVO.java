package com.zyzylong.seeksome.wallet.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包流水记录项。
 */
@Schema(description = "钱包流水记录项")
@Data
@Builder
public class AppWalletLogVO {

    @Schema(description = "流水 ID", example = "1")
    private Long walletLogId;

    @Schema(description = "金额（元）", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "类型：recharge-充值，freeze-冻结，unfreeze-解冻，deduct-扣除，pay-支付，income-入账",
            example = "recharge")
    private String type;

    @Schema(description = "关联业务类型：auction-拍品，order-订单", example = "auction")
    private String relatedType;

    @Schema(description = "关联业务 ID，配合 relatedType 使用", example = "1")
    private Long relatedId;

    @Schema(description = "记录时间", example = "2026-11-10T10:31:00")
    private LocalDateTime createTime;
}
