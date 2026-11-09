package com.zyzylong.seeksome.order.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端订单列表项。
 */
@Schema(description = "管理端订单列表项")
@Data
@Builder
public class AdminOrderVO {

    @Schema(description = "订单 ID", example = "1")
    private Long orderId;

    @Schema(description = "拍品 ID", example = "1")
    private Long auctionId;

    @Schema(description = "买家用户 ID", example = "1")
    private Long buyerId;

    @Schema(description = "卖家用户 ID", example = "2")
    private Long sellerId;

    @Schema(description = "成交价", example = "3520.00")
    private BigDecimal finalPrice;

    @Schema(description = "订单状态：pending_payment-待支付，paid-已支付，delivered-已交付，"
            + "completed-已完成，defaulted-已违约", example = "pending_payment")
    private String status;

    @Schema(description = "支付截止时间", example = "2026-11-09T10:30:00")
    private LocalDateTime payDeadline;

    @Schema(description = "支付时间", example = "2026-11-09T10:05:00")
    private LocalDateTime paidAt;

    @Schema(description = "交付时间", example = "2026-11-09T12:00:00")
    private LocalDateTime deliveredAt;

    @Schema(description = "完成时间", example = "2026-11-09T15:00:00")
    private LocalDateTime completedAt;

    @Schema(description = "下单时间", example = "2026-11-09T10:00:00")
    private LocalDateTime createTime;
}
