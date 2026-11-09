package com.zyzylong.seeksome.item.controller.admin;

import com.zyzylong.seeksome.framework.result.Result;
import com.zyzylong.seeksome.item.controller.admin.request.AdminCategorySaveRequest;
import com.zyzylong.seeksome.item.controller.admin.request.AdminCategorySortRequest;
import com.zyzylong.seeksome.item.controller.admin.vo.AdminCategoryVO;
import com.zyzylong.seeksome.item.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端分类接口。
 */
@Tag(name = "管理端-分类管理", description = "分类字典维护：列表、新增、排序与启停")
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分类列表", description = "返回全部分类（含禁用），按排序权重升序")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @GetMapping
    public Result<List<AdminCategoryVO>> listCategories() {
        return Result.success(categoryService.listCategories());
    }

    @Operation(summary = "新增分类", description = "编码全局唯一且创建后不可修改，新增分类默认启用")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @PostMapping
    public Result<Long> saveCategory(@Valid @RequestBody AdminCategorySaveRequest requestParam) {
        return Result.success(categoryService.saveCategory(requestParam));
    }

    @Operation(summary = "调整分类排序", description = "排序权重越小越靠前")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @PutMapping("/{categoryId}/sort")
    public Result<Void> updateCategorySort(@PathVariable Long categoryId,
                                           @Valid @RequestBody AdminCategorySortRequest requestParam) {
        categoryService.updateCategorySort(categoryId, requestParam.getSort());
        return Result.success(null);
    }

    @Operation(summary = "启用分类", description = "启用后用户端可见；重复启用幂等")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @PostMapping("/{categoryId}/enable")
    public Result<Void> enableCategory(@PathVariable Long categoryId) {
        categoryService.updateCategoryEnabled(categoryId, true);
        return Result.success(null);
    }

    @Operation(summary = "禁用分类", description = "禁用后用户端不可见，已有拍品引用保留；重复禁用幂等")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @PostMapping("/{categoryId}/disable")
    public Result<Void> disableCategory(@PathVariable Long categoryId) {
        categoryService.updateCategoryEnabled(categoryId, false);
        return Result.success(null);
    }
}
