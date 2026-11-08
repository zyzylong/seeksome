package com.zyzylong.seeksome.user.controller.admin;

import cn.hutool.crypto.digest.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyzylong.seeksome.auth.dao.entity.AdminUserDO;
import com.zyzylong.seeksome.auth.dao.mapper.AdminUserMapper;
import com.zyzylong.seeksome.user.dao.entity.UserDO;
import com.zyzylong.seeksome.user.dao.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminUserListApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    void list_requires_admin_login() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void list_returns_default_page_with_credit_and_wallet_overview() throws Exception {
        saveUser("user_old", "旧用户", 100, new BigDecimal("100.00"), new BigDecimal("0.00"),
                LocalDateTime.now().minusHours(2));
        UserDO newest = saveUser("user_new", "新用户", 80, new BigDecimal("80.00"), new BigDecimal("10.00"),
                LocalDateTime.now().minusHours(1));
        String token = loginAdmin();

        mockMvc.perform(get("/admin/users").header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].userId").value(newest.getId()))
                .andExpect(jsonPath("$.data.list[0].username").value("user_new"))
                .andExpect(jsonPath("$.data.list[0].nickname").value("新用户"))
                .andExpect(jsonPath("$.data.list[0].creditScore").value(80))
                .andExpect(jsonPath("$.data.list[0].walletBalance").value(80.00))
                .andExpect(jsonPath("$.data.list[0].frozenBalance").value(10.00))
                .andExpect(jsonPath("$.data.list[0].status").value("normal"))
                .andExpect(jsonPath("$.data.list[0].createdAt").exists())
                .andExpect(jsonPath("$.data.list[0].password").doesNotExist());
    }

    @Test
    void list_paginates_with_page_num_and_page_size() throws Exception {
        saveUser("user_p1", "分页一", 100, new BigDecimal("100.00"), BigDecimal.ZERO, LocalDateTime.now().minusHours(3));
        saveUser("user_p2", "分页二", 100, new BigDecimal("100.00"), BigDecimal.ZERO, LocalDateTime.now().minusHours(2));
        saveUser("user_p3", "分页三", 100, new BigDecimal("100.00"), BigDecimal.ZERO, LocalDateTime.now().minusHours(1));
        String token = loginAdmin();

        mockMvc.perform(get("/admin/users")
                        .header("admin-token", token)
                        .param("pageNum", "2")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].username").value("user_p1"));
    }

    @Test
    void list_filters_by_username_keyword() throws Exception {
        saveUser("user_zhangsan", "张三", 100, new BigDecimal("100.00"), BigDecimal.ZERO, LocalDateTime.now());
        saveUser("user_lisi", "李四", 100, new BigDecimal("100.00"), BigDecimal.ZERO, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(get("/admin/users").header("admin-token", token).param("keyword", "zhangsan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].username").value("user_zhangsan"));
    }

    @Test
    void list_filters_by_nickname_keyword_with_surrounding_blanks() throws Exception {
        saveUser("user_zhangsan", "张三", 100, new BigDecimal("100.00"), BigDecimal.ZERO, LocalDateTime.now());
        saveUser("user_lisi", "李四", 100, new BigDecimal("100.00"), BigDecimal.ZERO, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(get("/admin/users").header("admin-token", token).param("keyword", "  张三  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].nickname").value("张三"));
    }

    @Test
    void list_rejects_out_of_range_page_params() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(get("/admin/users").header("admin-token", token).param("pageNum", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        mockMvc.perform(get("/admin/users").header("admin-token", token).param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void list_rejects_empty_page_params() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(get("/admin/users").header("admin-token", token).param("pageNum", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        mockMvc.perform(get("/admin/users").header("admin-token", token).param("pageSize", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    private UserDO saveUser(String username, String nickname, int creditScore, BigDecimal walletBalance,
                            BigDecimal frozenBalance, LocalDateTime createdAt) {
        UserDO user = UserDO.builder()
                .username(username)
                .password(BCrypt.hashpw("123456", BCrypt.gensalt()))
                .nickname(nickname)
                .creditScore(creditScore)
                .walletBalance(walletBalance)
                .frozenBalance(frozenBalance)
                .status("normal")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
        userMapper.insert(user);
        return user;
    }

    private String loginAdmin() throws Exception {
        adminUserMapper.insert(AdminUserDO.builder()
                .username("admin_user_list")
                .password(BCrypt.hashpw("Passw0rd!", BCrypt.gensalt()))
                .role("ADMIN")
                .status(1)
                .failedCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        String body = objectMapper.writeValueAsString(
                Map.of("username", "admin_user_list", "password", "Passw0rd!"));
        MvcResult result = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").path("token").asText();
    }
}
