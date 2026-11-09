package com.zyzylong.seeksome.item.controller.admin;

import cn.hutool.crypto.digest.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyzylong.seeksome.auth.dao.entity.AdminUserDO;
import com.zyzylong.seeksome.auth.dao.mapper.AdminUserMapper;
import com.zyzylong.seeksome.item.dao.entity.CategoryDO;
import com.zyzylong.seeksome.item.dao.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminCategoryManageApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    void list_requires_admin_login() throws Exception {
        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void list_returns_all_categories_ordered_by_sort_and_id() throws Exception {
        CategoryDO lowerId = saveCategory("digital", "数码", 2, true);
        CategoryDO higherId = saveCategory("fashion", "穿搭", 2, true);
        CategoryDO disabled = saveCategory("legacy", "旧分类", 1, false);
        String token = loginAdmin();

        mockMvc.perform(get("/admin/categories").header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].categoryId").value(disabled.getId()))
                .andExpect(jsonPath("$.data[0].code").value("legacy"))
                .andExpect(jsonPath("$.data[0].name").value("旧分类"))
                .andExpect(jsonPath("$.data[0].sort").value(1))
                .andExpect(jsonPath("$.data[0].enabled").value(false))
                .andExpect(jsonPath("$.data[0].createTime").exists())
                .andExpect(jsonPath("$.data[0].updateTime").exists())
                .andExpect(jsonPath("$.data[1].categoryId").value(lowerId.getId()))
                .andExpect(jsonPath("$.data[1].enabled").value(true))
                .andExpect(jsonPath("$.data[2].categoryId").value(higherId.getId()));
    }

    @Test
    void create_requires_admin_login() throws Exception {
        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "digital", "name", "数码"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void create_saves_category_as_enabled_and_returns_id() throws Exception {
        String token = loginAdmin();

        MvcResult result = mockMvc.perform(post("/admin/categories")
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("code", "digital", "name", "数码", "sort", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data").isNumber())
                .andReturn();

        long categoryId = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").asLong();
        CategoryDO saved = categoryMapper.selectById(categoryId);
        assertNotNull(saved);
        assertEquals("digital", saved.getCode());
        assertEquals("数码", saved.getName());
        assertEquals(1, saved.getSort());
        assertTrue(saved.getEnabled());
    }

    @Test
    void create_trims_code_and_name() throws Exception {
        String token = loginAdmin();

        MvcResult result = mockMvc.perform(post("/admin/categories")
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", " digital ", "name", " 数码 "))))
                .andExpect(status().isOk())
                .andReturn();

        long categoryId = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").asLong();
        CategoryDO saved = categoryMapper.selectById(categoryId);
        assertEquals("digital", saved.getCode());
        assertEquals("数码", saved.getName());
    }

    @Test
    void create_defaults_sort_to_zero() throws Exception {
        String token = loginAdmin();

        MvcResult result = mockMvc.perform(post("/admin/categories")
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "digital", "name", "数码"))))
                .andExpect(status().isOk())
                .andReturn();

        long categoryId = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").asLong();
        assertEquals(0, categoryMapper.selectById(categoryId).getSort());
    }

    @Test
    void create_rejects_blank_code_or_name() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/admin/categories")
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "", "name", "数码"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));

        mockMvc.perform(post("/admin/categories")
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "digital", "name", "  "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void create_rejects_duplicate_code() throws Exception {
        saveCategory("digital", "数码", 1, true);
        String token = loginAdmin();

        mockMvc.perform(post("/admin/categories")
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("code", "digital", "name", "数码二手"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A0501"));
    }

    @Test
    void sort_update_requires_admin_login() throws Exception {
        CategoryDO category = saveCategory("digital", "数码", 1, true);

        mockMvc.perform(put("/admin/categories/{categoryId}/sort", category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("sort", 5))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void sort_update_changes_weight() throws Exception {
        CategoryDO category = saveCategory("digital", "数码", 1, true);
        String token = loginAdmin();

        mockMvc.perform(put("/admin/categories/{categoryId}/sort", category.getId())
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("sort", 9))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        assertEquals(9, categoryMapper.selectById(category.getId()).getSort());
    }

    @Test
    void sort_update_rejects_missing_or_negative_sort() throws Exception {
        CategoryDO category = saveCategory("digital", "数码", 1, true);
        String token = loginAdmin();

        mockMvc.perform(put("/admin/categories/{categoryId}/sort", category.getId())
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));

        mockMvc.perform(put("/admin/categories/{categoryId}/sort", category.getId())
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("sort", -1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void sort_update_unknown_category_returns_404() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(put("/admin/categories/{categoryId}/sort", 999999L)
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("sort", 1))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A0404"));
    }

    @Test
    void disable_requires_admin_login() throws Exception {
        CategoryDO category = saveCategory("digital", "数码", 1, true);

        mockMvc.perform(post("/admin/categories/{categoryId}/disable", category.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void disable_turns_category_off() throws Exception {
        CategoryDO category = saveCategory("digital", "数码", 1, true);
        String token = loginAdmin();

        mockMvc.perform(post("/admin/categories/{categoryId}/disable", category.getId())
                        .header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        assertFalse(categoryMapper.selectById(category.getId()).getEnabled());
    }

    @Test
    void disable_is_idempotent() throws Exception {
        CategoryDO category = saveCategory("digital", "数码", 1, false);
        String token = loginAdmin();

        mockMvc.perform(post("/admin/categories/{categoryId}/disable", category.getId())
                        .header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        assertFalse(categoryMapper.selectById(category.getId()).getEnabled());
    }

    @Test
    void enable_turns_category_on() throws Exception {
        CategoryDO category = saveCategory("digital", "数码", 1, false);
        String token = loginAdmin();

        mockMvc.perform(post("/admin/categories/{categoryId}/enable", category.getId())
                        .header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        assertTrue(categoryMapper.selectById(category.getId()).getEnabled());
    }

    @Test
    void enable_unknown_category_returns_404() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/admin/categories/{categoryId}/enable", 999999L)
                        .header("admin-token", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A0404"));
    }

    @Test
    void disable_unknown_category_returns_404() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/admin/categories/{categoryId}/disable", 999999L)
                        .header("admin-token", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A0404"));
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

    private String loginAdmin() throws Exception {
        adminUserMapper.insert(AdminUserDO.builder()
                .username("admin_category_manage")
                .password(BCrypt.hashpw("Passw0rd!", BCrypt.gensalt()))
                .role("ADMIN")
                .enabled(1)
                .failedCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build());
        String body = objectMapper.writeValueAsString(
                Map.of("username", "admin_category_manage", "password", "Passw0rd!"));
        MvcResult result = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").path("token").asText();
    }
}
