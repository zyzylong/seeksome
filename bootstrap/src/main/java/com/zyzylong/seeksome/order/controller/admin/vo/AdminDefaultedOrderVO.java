package com.zyzylong.seeksome.order.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端违约记录项，含保证金扣罚与信用分扣分明细。
 */
@Schema(description = "管理端违约记录项")
@Data
@Builder
public class AdminDefaultedOrderVO {

    @Schema(description = "订单 ID", example = "1")
    private Long orderId;

    @Schema(description = "拍品 ID", example = "1")
    private Long auctionId;

    @Schema(description = "买家用户 ID", example = "1")
    private Long buyerId;

    @Schema(description = "卖家用户 ID", example = "2")
    private Long sellerId;

    @Schema(description = "成交价", example = "2600.00")
    private BigDecimal finalPrice;

    @Schema(description = "支付截止时间", example = "2026-11-09T10:30:00")
    private LocalDateTime payDeadline;

    @Schema(description = "下单时间", example = "2026-11-09T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "保证金扣罚金额", example = "10.00")
    private BigDecimal depositDeductAmount;

    @Schema(description = "信用分变动值", example = "-20")
    private Integer creditChange;

    @Schema(description = "信用分变动原因", example = "超时未支付，违约扣分")
    private String creditReason;

    @Schema(description = "违约扣罚时间", example = "2026-11-09T10:31:00")
    private LocalDateTime penaltyTime;
}
