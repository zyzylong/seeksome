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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminUserFreezeApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    void freeze_requires_admin_login() throws Exception {
        Long userId = saveUser("user_freeze_anon", "normal").getId();

        mockMvc.perform(post("/admin/users/{userId}/freeze", userId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void unfreeze_requires_admin_login() throws Exception {
        Long userId = saveUser("user_unfreeze_anon", "frozen").getId();

        mockMvc.perform(post("/admin/users/{userId}/unfreeze", userId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void freeze_updates_status_to_frozen() throws Exception {
        Long userId = saveUser("user_freeze_ok", "normal").getId();
        String token = loginAdmin();

        mockMvc.perform(post("/admin/users/{userId}/freeze", userId).header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        assertEquals("frozen", userMapper.selectById(userId).getStatus());
    }

    @Test
    void unfreeze_updates_status_to_normal() throws Exception {
        Long userId = saveUser("user_unfreeze_ok", "frozen").getId();
        String token = loginAdmin();

        mockMvc.perform(post("/admin/users/{userId}/unfreeze", userId).header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        assertEquals("normal", userMapper.selectById(userId).getStatus());
    }

    @Test
    void freeze_unknown_user_returns_404() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/admin/users/{userId}/freeze", 999999L).header("admin-token", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A0404"));
    }

    @Test
    void unfreeze_unknown_user_returns_404() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/admin/users/{userId}/unfreeze", 999999L).header("admin-token", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A0404"));
    }

    @Test
    void freeze_is_idempotent_for_already_frozen_user() throws Exception {
        Long userId = saveUser("user_freeze_again", "frozen").getId();
        String token = loginAdmin();

        mockMvc.perform(post("/admin/users/{userId}/freeze", userId).header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        assertEquals("frozen", userMapper.selectById(userId).getStatus());
    }

    @Test
    void unfreeze_is_idempotent_for_normal_user() throws Exception {
        Long userId = saveUser("user_unfreeze_again", "normal").getId();
        String token = loginAdmin();

        mockMvc.perform(post("/admin/users/{userId}/unfreeze", userId).header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        assertEquals("normal", userMapper.selectById(userId).getStatus());
    }

    private UserDO saveUser(String username, String userStatus) {
        UserDO user = UserDO.builder()
                .username(username)
                .password(BCrypt.hashpw("123456", BCrypt.gensalt()))
                .nickname(username)
                .creditScore(100)
                .walletBalance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .status(userStatus)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);
        return user;
    }

    private String loginAdmin() throws Exception {
        adminUserMapper.insert(AdminUserDO.builder()
                .username("admin_user_freeze")
                .password(BCrypt.hashpw("Passw0rd!", BCrypt.gensalt()))
                .role("ADMIN")
                .enabled(1)
                .failedCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build());
        String body = objectMapper.writeValueAsString(
                Map.of("username", "admin_user_freeze", "password", "Passw0rd!"));
        MvcResult result = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").path("token").asText();
    }
}
