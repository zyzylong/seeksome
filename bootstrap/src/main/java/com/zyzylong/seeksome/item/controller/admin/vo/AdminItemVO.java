package com.zyzylong.seeksome.item.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端拍品列表项。
 */
@Schema(description = "管理端拍品列表项")
@Data
@Builder
public class AdminItemVO {

    @Schema(description = "拍品 ID", example = "1")
    private Long itemId;

    @Schema(description = "卖家用户 ID", example = "1")
    private Long sellerId;

    @Schema(description = "标题", example = "九成新 iPhone 15 Pro")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "分类 ID", example = "1")
    private Long categoryId;

    @Schema(description = "成色：95_new/90_new/80_new/flawed", example = "90_new")
    private String conditionLevel;

    @Schema(description = "图片地址，MVP 单图")
    private String images;

    @Schema(description = "起拍价", example = "4500.00")
    private BigDecimal startPrice;

    @Schema(description = "最小加价幅度", example = "100.00")
    private BigDecimal minIncrement;

    @Schema(description = "拍卖时长（小时）：24/48/72", example = "48")
    private Integer durationHours;

    @Schema(description = "当前价，无出价时为 NULL", example = "75.00")
    private BigDecimal currentPrice;

    @Schema(description = "开拍时间，待审核时为空", example = "2026-11-09T10:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间，待审核时为空", example = "2026-11-11T10:00:00")
    private LocalDateTime endTime;

    @Schema(description = "拍品状态：pending_review-待审核，active-在拍，ended-已结束，rejected-已驳回，cancelled-已下架",
            example = "pending_review")
    private String status;

    @Schema(description = "驳回原因，仅驳回状态非空", example = "描述与实际不符")
    private String rejectReason;

    @Schema(description = "提交时间", example = "2026-11-09T10:00:00")
    private LocalDateTime createTime;
}
