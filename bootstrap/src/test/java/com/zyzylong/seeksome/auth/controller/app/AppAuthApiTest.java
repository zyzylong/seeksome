package com.zyzylong.seeksome.auth.controller.app;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyzylong.seeksome.user.dao.entity.UserDO;
import com.zyzylong.seeksome.user.dao.mapper.UserMapper;
import com.zyzylong.seeksome.user.enums.UserStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppAuthApiTest {

    private static final String RAW_PASSWORD = "Passw0rd!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    void register_success_creates_user_with_hashed_password_and_defaults() throws Exception {
        MvcResult mvcResult = register("new_user", RAW_PASSWORD, "新用户")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andReturn();

        Long userId = readBody(mvcResult).path("data").asLong();
        UserDO saved = userMapper.selectById(userId);
        assertNotNull(saved);
        assertEquals("new_user", saved.getUsername());
        assertTrue(BCrypt.checkpw(RAW_PASSWORD, saved.getPassword()));
        assertEquals("新用户", saved.getNickname());
        assertEquals(100, saved.getCreditScore());
        assertEquals(0, BigDecimal.ZERO.compareTo(saved.getWalletBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(saved.getFrozenBalance()));
        assertEquals(UserStatusEnum.NORMAL.getValue(), saved.getStatus());
    }

    @Test
    void register_duplicate_username_returns_409() throws Exception {
        saveUser("dup_user");

        register("dup_user", RAW_PASSWORD, "重复用户")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A0206"));
    }

    @Test
    void register_trims_username_and_nickname() throws Exception {
        register("  trim_user  ", RAW_PASSWORD, "  昵称  ")
                .andExpect(status().isOk());

        UserDO saved = userMapper.selectOne(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, "trim_user"));
        assertNotNull(saved);
        assertEquals("昵称", saved.getNickname());
    }

    @Test
    void register_invalid_parameters_return_400() throws Exception {
        register(" ", RAW_PASSWORD, "昵称")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        register("valid_name", "123", "昵称")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        register("valid_name", RAW_PASSWORD, " ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        register("a".repeat(65), RAW_PASSWORD, "昵称")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        register("valid_name", RAW_PASSWORD, "a".repeat(65))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void login_success_returns_token_and_writes_satoken_header() throws Exception {
        UserDO user = saveUser("login_user");

        MvcResult mvcResult = login("login_user", RAW_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value("login_user"))
                .andExpect(jsonPath("$.data.nickname").value("昵称_login_user"))
                .andReturn();

        String token = readBody(mvcResult).path("data").path("token").asText();
        assertFalse(token.isBlank());
        assertEquals(token, mvcResult.getResponse().getHeader("satoken"));
    }

    @Test
    void login_wrong_password_and_unknown_user_share_same_error() throws Exception {
        saveUser("login_same");

        MvcResult wrongPassword = login("login_same", "bad-pass")
                .andExpect(status().isUnauthorized())
                .andReturn();
        MvcResult unknownUser = login("no_such_user", RAW_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andReturn();

        JsonNode wrongBody = readBody(wrongPassword);
        JsonNode unknownBody = readBody(unknownUser);
        assertEquals("A0205", wrongBody.path("code").asText());
        assertEquals(wrongBody.path("code").asText(), unknownBody.path("code").asText());
        assertEquals("用户名或密码错误", wrongBody.path("message").asText());
        assertEquals(wrongBody.path("message").asText(), unknownBody.path("message").asText());
    }

    @Test
    void login_frozen_user_returns_403() throws Exception {
        saveUser("login_frozen", UserStatusEnum.FROZEN.getValue());

        login("login_frozen", RAW_PASSWORD)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("A0301"));
    }

    @Test
    void login_invalid_parameters_return_400() throws Exception {
        login(" ", RAW_PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        login("login_user", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void logout_without_token_returns_401() throws Exception {
        mockMvc.perform(post("/app/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void logout_invalidates_token() throws Exception {
        saveUser("logout_user");
        MvcResult loginResult = login("logout_user", RAW_PASSWORD)
                .andExpect(status().isOk())
                .andReturn();
        String token = readBody(loginResult).path("data").path("token").asText();

        mockMvc.perform(post("/app/auth/logout").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));
        mockMvc.perform(post("/app/auth/logout").header("satoken", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    private ResultActions register(String username, String password, String nickname) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", username, "password", password, "nickname", nickname));
        return mockMvc.perform(post("/app/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions login(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        return mockMvc.perform(post("/app/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private JsonNode readBody(MvcResult mvcResult) throws Exception {
        return objectMapper.readTree(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private UserDO saveUser(String username) {
        return saveUser(username, UserStatusEnum.NORMAL.getValue());
    }

    private UserDO saveUser(String username, String userStatus) {
        UserDO user = UserDO.builder()
                .username(username)
                .password(BCrypt.hashpw(RAW_PASSWORD, BCrypt.gensalt()))
                .nickname("昵称_" + username)
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
}
