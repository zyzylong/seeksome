package com.zyzylong.seeksome.item.controller.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端拍品审核驳回请求。
 */
@Schema(description = "管理端拍品审核驳回请求")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminItemRejectRequest {

    @Schema(description = "驳回原因，卖家可见", example = "描述与实际不符", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 255)
    private String rejectReason;
}
