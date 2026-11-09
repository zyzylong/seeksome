package com.zyzylong.seeksome.item.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端分类列表项。
 */
@Schema(description = "管理端分类列表项")
@Data
@Builder
public class AdminCategoryVO {

    @Schema(description = "分类 ID", example = "1")
    private Long categoryId;

    @Schema(description = "分类编码", example = "digital")
    private String code;

    @Schema(description = "分类名称", example = "数码")
    private String name;

    @Schema(description = "排序权重，越小越靠前", example = "1")
    private Integer sort;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间", example = "2026-11-09T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-11-09T10:00:00")
    private LocalDateTime updateTime;
}
