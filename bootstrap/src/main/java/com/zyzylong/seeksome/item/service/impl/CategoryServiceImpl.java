package com.zyzylong.seeksome.item.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.item.controller.admin.request.AdminCategorySaveRequest;
import com.zyzylong.seeksome.item.controller.admin.vo.AdminCategoryVO;
import com.zyzylong.seeksome.item.controller.app.vo.AppCategoryVO;
import com.zyzylong.seeksome.item.dao.entity.CategoryDO;
import com.zyzylong.seeksome.item.dao.mapper.CategoryMapper;
import com.zyzylong.seeksome.item.enums.ItemErrorCodeEnum;
import com.zyzylong.seeksome.item.exception.ItemException;
import com.zyzylong.seeksome.item.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拍品分类服务实现。
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<AdminCategoryVO> listCategories() {
        List<CategoryDO> categories = categoryMapper.selectList(new LambdaQueryWrapper<CategoryDO>()
                .orderByAsc(CategoryDO::getSort)
                .orderByAsc(CategoryDO::getId));
        return categories.stream().map(this::toAdminCategoryVO).toList();
    }

    @Override
    public List<AppCategoryVO> listEnabledCategories() {
        List<CategoryDO> categories = categoryMapper.selectList(new LambdaQueryWrapper<CategoryDO>()
                .eq(CategoryDO::getEnabled, true)
                .orderByAsc(CategoryDO::getSort)
                .orderByAsc(CategoryDO::getId));
        return categories.stream().map(this::toAppCategoryVO).toList();
    }

    @Override
    public Long saveCategory(AdminCategorySaveRequest requestParam) {
        String code = StrUtil.trim(requestParam.getCode());
        String name = StrUtil.trim(requestParam.getName());
        Long duplicatedCount = categoryMapper.selectCount(new LambdaQueryWrapper<CategoryDO>()
                .eq(CategoryDO::getCode, code));
        if (duplicatedCount > 0) {
            throw new ItemException(ItemErrorCodeEnum.CATEGORY_CODE_EXISTS);
        }
        CategoryDO category = CategoryDO.builder()
                .code(code)
                .name(name)
                .sort(requestParam.getSort())
                .enabled(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        try {
            categoryMapper.insert(category);
        } catch (DuplicateKeyException ex) {
            throw new ItemException(ItemErrorCodeEnum.CATEGORY_CODE_EXISTS);
        }
        return category.getId();
    }

    @Override
    public void updateCategorySort(Long categoryId, Integer sort) {
        CategoryDO category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new ItemException(CommonErrorCodeEnum.NOT_FOUND);
        }
        categoryMapper.update(null, new LambdaUpdateWrapper<CategoryDO>()
                .eq(CategoryDO::getId, categoryId)
                .set(CategoryDO::getSort, sort)
                .set(CategoryDO::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public void updateCategoryEnabled(Long categoryId, Boolean enabled) {
        CategoryDO category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new ItemException(CommonErrorCodeEnum.NOT_FOUND);
        }
        if (enabled.equals(category.getEnabled())) {
            return;
        }
        categoryMapper.update(null, new LambdaUpdateWrapper<CategoryDO>()
                .eq(CategoryDO::getId, categoryId)
                .set(CategoryDO::getEnabled, enabled)
                .set(CategoryDO::getUpdateTime, LocalDateTime.now()));
    }

    private AdminCategoryVO toAdminCategoryVO(CategoryDO category) {
        return AdminCategoryVO.builder()
                .categoryId(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .sort(category.getSort())
                .enabled(category.getEnabled())
                .createTime(category.getCreateTime())
                .updateTime(category.getUpdateTime())
                .build();
    }

    private AppCategoryVO toAppCategoryVO(CategoryDO category) {
        return AppCategoryVO.builder()
                .categoryId(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .build();
    }
}
