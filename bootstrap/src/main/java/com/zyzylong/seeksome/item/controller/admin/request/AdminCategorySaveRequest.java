package com.zyzylong.seeksome.item.controller.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理端新增分类请求。
 */
@Schema(description = "管理端新增分类请求")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategorySaveRequest {

    @Schema(description = "分类编码，全局唯一，创建后不可修改", example = "digital")
    @NotBlank
    @Size(max = 32)
    private String code;

    @Schema(description = "分类名称", example = "数码")
    @NotBlank
    @Size(max = 32)
    private String name;

    @Schema(description = "排序权重，越小越靠前", example = "1")
    @Min(0)
    @Builder.Default
    private Integer sort = 0;
}
