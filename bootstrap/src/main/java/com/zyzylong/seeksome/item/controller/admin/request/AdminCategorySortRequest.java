package com.zyzylong.seeksome.item.controller.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端分类排序更新请求。
 */
@Schema(description = "管理端分类排序更新请求")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategorySortRequest {

    @Schema(description = "排序权重，越小越靠前", example = "1")
    @NotNull
    @Min(0)
    private Integer sort;
}
