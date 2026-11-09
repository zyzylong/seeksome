package com.zyzylong.seeksome.item.controller.app;

import com.zyzylong.seeksome.item.dao.entity.CategoryDO;
import com.zyzylong.seeksome.item.dao.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppCategoryListApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    void list_is_public_and_returns_only_enabled_categories_ordered_by_sort_and_id() throws Exception {
        CategoryDO second = saveCategory("fashion", "穿搭", 2, true);
        CategoryDO firstLowerId = saveCategory("digital", "数码", 1, true);
        CategoryDO firstHigherId = saveCategory("home", "家居", 1, true);
        saveCategory("legacy", "旧分类", 0, false);

        mockMvc.perform(get("/app/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].categoryId").value(firstLowerId.getId()))
                .andExpect(jsonPath("$.data[0].code").value("digital"))
                .andExpect(jsonPath("$.data[0].name").value("数码"))
                .andExpect(jsonPath("$.data[0].enabled").doesNotExist())
                .andExpect(jsonPath("$.data[0].createTime").doesNotExist())
                .andExpect(jsonPath("$.data[1].categoryId").value(firstHigherId.getId()))
                .andExpect(jsonPath("$.data[2].categoryId").value(second.getId()));
    }

    @Test
    void list_returns_empty_when_no_enabled_categories() throws Exception {
        saveCategory("legacy", "旧分类", 1, false);

        mockMvc.perform(get("/app/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    private CategoryDO saveCategory(String code, String name, int sort, boolean enabled) {
        CategoryDO category = CategoryDO.builder()
                .code(code)
                .name(name)
                .sort(sort)
                .enabled(enabled)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        categoryMapper.insert(category);
        return category;
    }
}
