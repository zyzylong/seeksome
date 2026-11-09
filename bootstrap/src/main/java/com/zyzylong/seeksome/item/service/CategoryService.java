package com.zyzylong.seeksome.item.service;

import com.zyzylong.seeksome.item.controller.admin.request.AdminCategorySaveRequest;
import com.zyzylong.seeksome.item.controller.admin.vo.AdminCategoryVO;
import com.zyzylong.seeksome.item.controller.app.vo.AppCategoryVO;

import java.util.List;

/**
 * 拍品分类服务。
 */
public interface CategoryService {

    List<AdminCategoryVO> listCategories();

    List<AppCategoryVO> listEnabledCategories();

    Long saveCategory(AdminCategorySaveRequest requestParam);

    void updateCategorySort(Long categoryId, Integer sort);

    void updateCategoryEnabled(Long categoryId, Boolean enabled);
}
