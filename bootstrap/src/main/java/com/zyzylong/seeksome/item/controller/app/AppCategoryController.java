package com.zyzylong.seeksome.item.controller.app;

import com.zyzylong.seeksome.framework.result.Result;
import com.zyzylong.seeksome.item.controller.app.vo.AppCategoryVO;
import com.zyzylong.seeksome.item.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端分类接口。
 */
@Tag(name = "用户端-分类", description = "用户端分类列表查询")
@RestController
@RequestMapping("/app/categories")
@RequiredArgsConstructor
public class AppCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分类列表", description = "返回全部启用分类，按排序权重升序；无需登录")
    @GetMapping
    public Result<List<AppCategoryVO>> listCategories() {
        return Result.success(categoryService.listEnabledCategories());
    }
}
