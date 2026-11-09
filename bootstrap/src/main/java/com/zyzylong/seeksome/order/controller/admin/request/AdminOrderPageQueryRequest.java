package com.zyzylong.seeksome.order.controller.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端订单分页查询请求。
 */
@Schema(description = "管理端订单分页查询请求")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderPageQueryRequest {

    @Schema(description = "页码，从 1 开始", example = "1")
    @NotNull
    @Min(1)
    @Builder.Default
    private Integer pageNum = 1;

    @Schema(description = "每页条数，最大 100", example = "10")
    @NotNull
    @Min(1)
    @Max(100)
    @Builder.Default
    private Integer pageSize = 10;

    @Schema(description = "订单状态：pending_payment/paid/delivered/completed/defaulted，不传查全部",
            example = "pending_payment")
    @Size(max = 16)
    private String status;
}
