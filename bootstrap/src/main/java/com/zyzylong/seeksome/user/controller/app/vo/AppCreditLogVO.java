package com.zyzylong.seeksome.user.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信用分变动记录项。
 */
@Schema(description = "信用分变动记录项")
@Data
@Builder
public class AppCreditLogVO {

    @Schema(description = "记录 ID", example = "1")
    private Long creditLogId;

    @Schema(description = "信用分变动值，违约扣分为负数", example = "-20")
    private Integer changeValue;

    @Schema(description = "变动原因", example = "超时未支付，违约扣分")
    private String reason;

    @Schema(description = "关联业务类型：order-订单", example = "order")
    private String relatedType;

    @Schema(description = "关联业务 ID，配合 relatedType 使用", example = "1")
    private Long relatedId;

    @Schema(description = "记录时间", example = "2026-11-09T10:31:00")
    private LocalDateTime createTime;
}
