package com.zyzylong.seeksome.wallet.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端保证金流水列表项。
 */
@Schema(description = "管理端保证金流水列表项")
@Data
@Builder
public class AdminDepositLogVO {

    @Schema(description = "流水 ID", example = "1")
    private Long depositLogId;

    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    @Schema(description = "拍品 ID", example = "1")
    private Long auctionId;

    @Schema(description = "金额", example = "10.00")
    private BigDecimal amount;

    @Schema(description = "类型：freeze-冻结，unfreeze-解冻，deduct-扣除", example = "deduct")
    private String type;

    @Schema(description = "记录时间", example = "2026-11-09T10:00:00")
    private LocalDateTime createTime;
}
