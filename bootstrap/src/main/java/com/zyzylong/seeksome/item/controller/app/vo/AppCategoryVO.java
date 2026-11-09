package com.zyzylong.seeksome.item.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户端分类列表项。
 */
@Schema(description = "用户端分类列表项")
@Data
@Builder
public class AppCategoryVO {

    @Schema(description = "分类 ID", example = "1")
    private Long categoryId;

    @Schema(description = "分类编码", example = "digital")
    private String code;

    @Schema(description = "分类名称", example = "数码")
    private String name;
}
