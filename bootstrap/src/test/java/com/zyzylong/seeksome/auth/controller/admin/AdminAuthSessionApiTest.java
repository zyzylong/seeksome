package com.zyzylong.seeksome.auth.controller.admin;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyzylong.seeksome.auth.dao.entity.AdminUserDO;
import com.zyzylong.seeksome.auth.dao.mapper.AdminUserMapper;
import com.zyzylong.seeksome.auth.service.impl.AdminAuthServiceImpl;
import com.zyzylong.seeksome.support.UserSessionProbeController;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(UserSessionProbeController.class)
class AdminAuthSessionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void admin_and_user_sessions_are_isolated() throws Exception {
        saveAdmin("admin_iso", "Passw0rd!");
        String adminToken = readToken(login("admin_iso", "Passw0rd!", "10.3.0.1")
                .andExpect(status().isOk()).andReturn());
        MvcResult userLogin = mockMvc.perform(get("/test/user/login").param("userId", "1001"))
                .andExpect(status().isOk()).andReturn();
        String userToken = userLogin.getResponse().getHeader("satoken");
        assertNotNull(userToken);
        mockMvc.perform(get("/admin/auth/me").header("admin-token", userToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/test/user/me").header("satoken", adminToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_and_logout_without_token_return_401() throws Exception {
        mockMvc.perform(get("/admin/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
        mockMvc.perform(post("/admin/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_invalidates_token_and_writes_audit_log() throws Exception {
        saveAdmin("admin_logout", "Passw0rd!");
        String token = readToken(login("admin_logout", "Passw0rd!", "10.3.0.2")
                .andExpect(status().isOk()).andReturn());
        mockMvc.perform(get("/admin/auth/me").header("admin-token", token)).andExpect(status().isOk());
        Logger logger = (Logger) LoggerFactory.getLogger(AdminAuthServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            mockMvc.perform(post("/admin/auth/logout")
                            .header("admin-token", token)
                            .with(request -> {
                                request.setRemoteAddr("10.3.0.2");
                                return request;
                            }))
                    .andExpect(status().isOk());
            String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
            assertTrue(logs.contains("event=logout"));
            assertTrue(logs.contains("ip=10.3.0.2"));
        } finally {
            logger.detachAppender(appender);
        }
        mockMvc.perform(get("/admin/auth/me").header("admin-token", token)).andExpect(status().isUnauthorized());
    }

    @Test
    void disabled_admin_session_rejected_on_me() throws Exception {
        AdminUserDO admin = saveAdmin("admin_disable_later", "Passw0rd!");
        String token = readToken(login("admin_disable_later", "Passw0rd!", "10.3.0.3")
                .andExpect(status().isOk()).andReturn());
        adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUserDO>()
                .eq(AdminUserDO::getId, admin.getId())
                .set(AdminUserDO::getEnabled, 0));
        mockMvc.perform(get("/admin/auth/me").header("admin-token", token))
                .andExpect(status().isUnauthorized());
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
        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body).path("data").path("token").asText();
    }

    private AdminUserDO saveAdmin(String username, String rawPassword) {
        AdminUserDO admin = AdminUserDO.builder()
                .username(username)
                .password(BCrypt.hashpw(rawPassword, BCrypt.gensalt()))
                .role("ADMIN")
                .enabled(1)
                .failedCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        adminUserMapper.insert(admin);
        return admin;
    }
}
