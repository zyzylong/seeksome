package com.zyzylong.seeksome.admin.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyzylong.seeksome.admin.domain.dataobject.AdminUserDO;
import com.zyzylong.seeksome.admin.mapper.AdminUserMapper;
import com.zyzylong.seeksome.admin.service.impl.AdminAuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminAuthLoginApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_success_returns_token_and_me_works() throws Exception {
        saveAdmin("admin_ok", "Passw0rd!");
        MvcResult loginResult = login("admin_ok", "Passw0rd!", "10.2.0.1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.username").value("admin_ok"))
                .andReturn();
        String token = readToken(loginResult);
        mockMvc.perform(get("/admin/auth/me").header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin_ok"));
    }

    @Test
    void wrong_password_and_unknown_account_return_identical_body() throws Exception {
        saveAdmin("admin_same", "Passw0rd!");
        MvcResult wrongResult = login("admin_same", "bad-pass", "10.2.0.2")
                .andExpect(status().isUnauthorized()).andReturn();
        MvcResult unknownResult = login("no_such_user", "bad-pass", "10.2.0.2")
                .andExpect(status().isUnauthorized()).andReturn();
        JsonNode wrong = readBody(wrongResult);
        JsonNode unknown = readBody(unknownResult);
        assertEquals(wrong.path("code").asText(), unknown.path("code").asText());
        assertEquals("A0201", wrong.path("code").asText());
        assertEquals(wrong.path("message").asText(), unknown.path("message").asText());
        assertEquals("用户名或密码错误", wrong.path("message").asText());
        assertTrue(wrong.path("data").isNull());
    }

    @Test
    void account_locked_after_five_failures_and_sixth_rejects_correct_password() throws Exception {
        AdminUserDO admin = saveAdmin("admin_locked", "Passw0rd!");
        for (int i = 0; i < 5; i++) {
            login("admin_locked", "bad-pass", "10.2.0.3").andExpect(status().isUnauthorized());
        }
        login("admin_locked", "Passw0rd!", "10.2.0.3")
                .andExpect(status().is(423))
                .andExpect(jsonPath("$.code").value("A0202"));
        AdminUserDO result = adminUserMapper.selectById(admin.getId());
        assertEquals(5, result.getFailedCount());
        assertNotNull(result.getLockedUntil());
    }

    @Test
    void disabled_account_returns_403() throws Exception {
        saveAdmin("admin_disabled", "Passw0rd!", 0);
        login("admin_disabled", "Passw0rd!", "10.2.0.4")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("A0203"));
    }

    @Test
    void ip_rate_limited_after_twenty_failures() throws Exception {
        saveAdmin("admin_rate_api", "Passw0rd!");
        for (int i = 0; i < 20; i++) {
            String eachUsername = "no_such_" + i;
            login(eachUsername, "bad-pass", "10.2.0.5").andExpect(status().isUnauthorized());
        }
        login("admin_rate_api", "Passw0rd!", "10.2.0.5")
                .andExpect(status().is(429))
                .andExpect(jsonPath("$.code").value("A0204"));
    }

    @Test
    void invalid_parameters_return_400() throws Exception {
        login(" ", "", "10.2.0.6")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void overlong_credentials_return_400() throws Exception {
        login("a".repeat(65), "Passw0rd!", "10.2.0.7")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void login_after_lock_expiry_succeeds() throws Exception {
        AdminUserDO admin = saveAdmin("admin_expired_api", "Passw0rd!");
        adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUserDO>()
                .eq(AdminUserDO::getId, admin.getId())
                .set(AdminUserDO::getFailedCount, 5)
                .set(AdminUserDO::getLockedUntil, LocalDateTime.now().minusMinutes(1)));
        login("admin_expired_api", "Passw0rd!", "10.2.0.8").andExpect(status().isOk());
        AdminUserDO result = adminUserMapper.selectById(admin.getId());
        assertEquals(0, result.getFailedCount());
        assertNull(result.getLockedUntil());
    }

    @Test
    void login_success_writes_audit_log() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(AdminAuthServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            saveAdmin("admin_audit", "Passw0rd!");
            login("admin_audit", "Passw0rd!", "10.2.0.9")
                    .andExpect(status().isOk());
            String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
            assertTrue(logs.contains("event=login-success"));
            assertTrue(logs.contains("ip=10.2.0.9"));
            assertTrue(logs.contains("ua=JUnit-Agent"));
        } finally {
            logger.detachAppender(appender);
        }
    }

    private ResultActions login(String username, String password, String clientIp) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        return mockMvc.perform(post("/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "JUnit-Agent")
                .content(body)
                .with(request -> {
                    request.setRemoteAddr(clientIp);
                    return request;
                }));
    }

    private String readToken(MvcResult mvcResult) throws Exception {
        return readBody(mvcResult).path("data").path("token").asText();
    }

    private JsonNode readBody(MvcResult mvcResult) throws Exception {
        return objectMapper.readTree(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private AdminUserDO saveAdmin(String username, String rawPassword) {
        return saveAdmin(username, rawPassword, 1);
    }

    private AdminUserDO saveAdmin(String username, String rawPassword, int status) {
        AdminUserDO admin = AdminUserDO.builder()
                .username(username)
                .password(BCrypt.hashpw(rawPassword, BCrypt.gensalt()))
                .role("ADMIN")
                .status(status)
                .failedCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        adminUserMapper.insert(admin);
        return admin;
    }
}
