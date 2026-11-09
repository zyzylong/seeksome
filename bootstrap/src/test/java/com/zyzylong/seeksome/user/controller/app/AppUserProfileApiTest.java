package com.zyzylong.seeksome.user.controller.app;

import cn.hutool.crypto.digest.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppUserProfileApiTest {

    private static final String PASSWORD_HASH = BCrypt.hashpw("123456", BCrypt.gensalt());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    void getProfile_requires_login() throws Exception {
        mockMvc.perform(get("/app/users/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void getProfile_returns_current_user_info() throws Exception {
        UserDO user = saveUser("profile_user", "小明", "/files/avatar/1.png", 80);
        String token = login(user.getId());

        mockMvc.perform(get("/app/users/profile").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.userId").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value("profile_user"))
                .andExpect(jsonPath("$.data.nickname").value("小明"))
                .andExpect(jsonPath("$.data.avatar").value("/files/avatar/1.png"))
                .andExpect(jsonPath("$.data.creditScore").value(80))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void getProfile_is_scoped_to_logged_in_user() throws Exception {
        saveUser("profile_other", "其他用户", null, 100);
        UserDO current = saveUser("profile_current", "当前用户", null, 60);
        String token = login(current.getId());

        mockMvc.perform(get("/app/users/profile").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(current.getId()))
                .andExpect(jsonPath("$.data.username").value("profile_current"))
                .andExpect(jsonPath("$.data.creditScore").value(60));
    }

    @Test
    void getProfile_unknown_user_returns_404() throws Exception {
        String token = login(999999L);

        mockMvc.perform(get("/app/users/profile").header("satoken", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A0404"));
    }

    @Test
    void updateProfile_updates_nickname_and_avatar() throws Exception {
        UserDO user = saveUser("update_user", "旧昵称", null, 100);
        String token = login(user.getId());

        updateProfile(token, "新昵称", "https://cdn.example.com/avatar/new.png")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.avatar").value("https://cdn.example.com/avatar/new.png"))
                .andExpect(jsonPath("$.data.username").value("update_user"))
                .andExpect(jsonPath("$.data.creditScore").value(100));

        UserDO saved = userMapper.selectById(user.getId());
        assertEquals("新昵称", saved.getNickname());
        assertEquals("https://cdn.example.com/avatar/new.png", saved.getAvatar());
        assertEquals("update_user", saved.getUsername());
        assertEquals(100, saved.getCreditScore());
    }

    @Test
    void updateProfile_trims_nickname_and_avatar() throws Exception {
        UserDO user = saveUser("trim_profile", "旧昵称", null, 100);
        String token = login(user.getId());

        updateProfile(token, "  新昵称  ", "  /files/avatar/trim.png  ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.avatar").value("/files/avatar/trim.png"));
    }

    @Test
    void updateProfile_keeps_avatar_when_not_provided() throws Exception {
        UserDO user = saveUser("keep_avatar", "旧昵称", "/files/avatar/old.png", 100);
        String token = login(user.getId());

        updateProfile(token, "新昵称", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avatar").value("/files/avatar/old.png"));

        assertEquals("/files/avatar/old.png", userMapper.selectById(user.getId()).getAvatar());
    }

    @Test
    void updateProfile_clears_avatar_when_blank() throws Exception {
        UserDO user = saveUser("clear_avatar", "旧昵称", "/files/avatar/old.png", 100);
        String token = login(user.getId());

        updateProfile(token, "新昵称", "   ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.avatar").value(nullValue()));

        assertNull(userMapper.selectById(user.getId()).getAvatar());
    }

    @Test
    void updateProfile_requires_login() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("nickname", "新昵称"));

        mockMvc.perform(put("/app/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void updateProfile_rejects_invalid_parameters() throws Exception {
        UserDO user = saveUser("invalid_profile", "旧昵称", null, 100);
        String token = login(user.getId());

        updateProfile(token, " ", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        updateProfile(token, "a".repeat(65), null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        updateProfile(token, "新昵称", "a".repeat(256))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void updateProfile_rejects_missing_nickname() throws Exception {
        UserDO user = saveUser("missing_nickname", "旧昵称", null, 100);
        String token = login(user.getId());
        String body = objectMapper.writeValueAsString(Map.of("avatar", "/files/avatar/1.png"));

        mockMvc.perform(put("/app/users/profile")
                        .header("satoken", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    private ResultActions updateProfile(String token, String nickname, String avatar) throws Exception {
        Map<String, Object> body = new HashMap<>();
        if (nickname != null) {
            body.put("nickname", nickname);
        }
        if (avatar != null) {
            body.put("avatar", avatar);
        }
        return mockMvc.perform(put("/app/users/profile")
                .header("satoken", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private UserDO saveUser(String username, String nickname, String avatar, int creditScore) {
        LocalDateTime now = LocalDateTime.now();
        UserDO user = UserDO.builder()
                .username(username)
                .password(PASSWORD_HASH)
                .nickname(nickname)
                .avatar(avatar)
                .creditScore(creditScore)
                .walletBalance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .status("normal")
                .createTime(now)
                .updateTime(now)
                .build();
        userMapper.insert(user);
        return user;
    }

    private String login(Long userId) throws Exception {
        MvcResult result = mockMvc.perform(get("/test/user/login").param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getHeader("satoken");
    }
}
