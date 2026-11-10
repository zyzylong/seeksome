package com.zyzylong.seeksome.item.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zyzylong.seeksome.item.controller.admin.request.AdminCategorySaveRequest;
import com.zyzylong.seeksome.item.dao.entity.CategoryDO;
import com.zyzylong.seeksome.item.dao.mapper.CategoryMapper;
import com.zyzylong.seeksome.item.enums.ItemErrorCodeEnum;
import com.zyzylong.seeksome.item.exception.ItemException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoSpyBean
    private CategoryMapper categoryMapper;

    @Test
    void saveCategory_rejects_duplicate_code_when_concurrent_insert_wins() {
        saveCategory("digital", "数码");
        doReturn(0L).when(categoryMapper).selectCount(any());

        ItemException exception = assertThrows(ItemException.class,
                () -> categoryService.saveCategory(AdminCategorySaveRequest.builder()
                        .code("digital")
                        .name("数码二手")
                        .sort(0)
                        .build()));

        assertEquals(ItemErrorCodeEnum.CATEGORY_CODE_EXISTS, exception.getErrorCode());
        assertEquals(1, selectCategoriesByCode("digital").size());
    }

    private void saveCategory(String code, String name) {
        LocalDateTime now = LocalDateTime.now();
        categoryMapper.insert(CategoryDO.builder()
                .code(code)
                .name(name)
                .sort(0)
                .enabled(true)
                .createTime(now)
                .updateTime(now)
                .build());
    }

    private List<CategoryDO> selectCategoriesByCode(String code) {
        return categoryMapper.selectList(new LambdaQueryWrapper<CategoryDO>()
                .eq(CategoryDO::getCode, code));
    }
}
