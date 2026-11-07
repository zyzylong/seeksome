package com.zyzylong.seeksome.admin.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zyzylong.seeksome.admin.domain.dataobject.AdminUserDO;
import com.zyzylong.seeksome.admin.domain.dto.AdminLoginReqDTO;
import com.zyzylong.seeksome.admin.exception.AdminAuthException;
import com.zyzylong.seeksome.admin.mapper.AdminUserMapper;
import com.zyzylong.seeksome.admin.service.impl.AdminAuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminAuthServiceTest {

    @Autowired
    private AdminAuthService adminAuthService;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Test
    void wrong_password_returns_generic_login_failure_and_counts_failure() {
        AdminUserDO admin = saveAdmin("admin_wrong", "Passw0rd!");
        AdminAuthException ex = assertThrows(AdminAuthException.class,
                () -> adminAuthService.login(loginReq("admin_wrong", "bad-pass"), "10.1.0.1", "JUnit"));
        assertEquals("A0201", ex.getErrorCode().getCode());
        assertEquals(1, adminUserMapper.selectById(admin.getId()).getFailedCount());
    }

    @Test
    void unknown_account_returns_identical_error_code_and_message() {
        saveAdmin("admin_same", "Passw0rd!");
        AdminAuthException wrong = assertThrows(AdminAuthException.class,
                () -> adminAuthService.login(loginReq("admin_same", "bad-pass"), "10.1.0.2", "JUnit"));
        AdminAuthException unknown = assertThrows(AdminAuthException.class,
                () -> adminAuthService.login(loginReq("no_such_admin", "bad-pass"), "10.1.0.2", "JUnit"));
        assertEquals(wrong.getErrorCode().getCode(), unknown.getErrorCode().getCode());
        assertEquals(wrong.getErrorCode().getMessage(), unknown.getErrorCode().getMessage());
    }

    @Test
    void five_failures_lock_account_and_sixth_rejects_correct_password() {
        AdminUserDO admin = saveAdmin("admin_lock", "Passw0rd!");
        Logger logger = (Logger) LoggerFactory.getLogger(AdminAuthServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            for (int i = 0; i < 5; i++) {
                assertThrows(AdminAuthException.class,
                        () -> adminAuthService.login(loginReq("admin_lock", "bad-pass"), "10.1.0.3", "JUnit"));
            }
            String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
            assertTrue(logs.contains("event=account-locked"));
            assertTrue(logs.contains("reason=threshold-reached"));
        } finally {
            logger.detachAppender(appender);
        }
        AdminUserDO locked = adminUserMapper.selectById(admin.getId());
        assertEquals(5, locked.getFailedCount());
        assertNotNull(locked.getLockedUntil());
        AdminAuthException ex = assertThrows(AdminAuthException.class,
                () -> adminAuthService.login(loginReq("admin_lock", "Passw0rd!"), "10.1.0.3", "JUnit"));
        assertEquals("A0202", ex.getErrorCode().getCode());
        assertEquals(5, adminUserMapper.selectById(admin.getId()).getFailedCount());
    }

    @Test
    void disabled_account_with_correct_password_returns_disabled() {
        saveAdmin("admin_disabled", "Passw0rd!", 0);
        AdminAuthException ex = assertThrows(AdminAuthException.class,
                () -> adminAuthService.login(loginReq("admin_disabled", "Passw0rd!"), "10.1.0.4", "JUnit"));
        assertEquals("A0203", ex.getErrorCode().getCode());
    }

    @Test
    void login_after_lock_expiry_clears_lock() {
        AdminUserDO admin = saveAdmin("admin_expired", "Passw0rd!");
        adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUserDO>()
                .eq(AdminUserDO::getId, admin.getId())
                .set(AdminUserDO::getFailedCount, 4)
                .set(AdminUserDO::getLockedUntil, LocalDateTime.now().minusMinutes(1)));
        AdminAuthException ex = assertThrows(AdminAuthException.class,
                () -> adminAuthService.login(loginReq("admin_expired", "bad-pass"), "10.1.0.5", "JUnit"));
        assertEquals("A0201", ex.getErrorCode().getCode());
        AdminUserDO result = adminUserMapper.selectById(admin.getId());
        assertEquals(1, result.getFailedCount());
        assertNull(result.getLockedUntil());
    }

    @Test
    void corrupted_password_hash_returns_generic_failure() {
        AdminUserDO admin = saveAdmin("admin_bad_hash", "Passw0rd!");
        adminUserMapper.update(null, new LambdaUpdateWrapper<AdminUserDO>()
                .eq(AdminUserDO::getId, admin.getId())
                .set(AdminUserDO::getPassword, "not-a-bcrypt-hash"));
        AdminAuthException ex = assertThrows(AdminAuthException.class,
                () -> adminAuthService.login(loginReq("admin_bad_hash", "Passw0rd!"), "10.1.0.6", "JUnit"));
        assertEquals("A0201", ex.getErrorCode().getCode());
    }

    @Test
    void ip_rate_limited_blocks_even_valid_credentials() {
        saveAdmin("admin_rate", "Passw0rd!");
        for (int i = 0; i < 20; i++) {
            String eachUsername = "no_such_" + i;
            assertThrows(AdminAuthException.class,
                    () -> adminAuthService.login(loginReq(eachUsername, "bad-pass"), "10.1.0.7", "JUnit"));
        }
        AdminAuthException ex = assertThrows(AdminAuthException.class,
                () -> adminAuthService.login(loginReq("admin_rate", "Passw0rd!"), "10.1.0.7", "JUnit"));
        assertEquals("A0204", ex.getErrorCode().getCode());
    }

    @Test
    void unknown_account_still_runs_bcrypt_to_avoid_timing_side_channel() {
        assertThrows(AdminAuthException.class,
                () -> adminAuthService.login(loginReq("timing_warmup", "bad-pass"), "10.1.0.9", "JUnit"));
        long startNanos = System.nanoTime();
        assertThrows(AdminAuthException.class,
                () -> adminAuthService.login(loginReq("no_such_timing", "bad-pass"), "10.1.0.9", "JUnit"));
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
        assertTrue(elapsedMillis >= 10,
                "unknown-account login must still run a BCrypt check to avoid a timing side channel, took "
                        + elapsedMillis + "ms");
    }

    @Test
    void missing_user_agent_does_not_break_failure_audit() {
        Logger logger = (Logger) LoggerFactory.getLogger(AdminAuthServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThrows(AdminAuthException.class,
                    () -> adminAuthService.login(loginReq("no_such_ua", "bad-pass"), "10.1.0.8", null));
            String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(Collectors.joining("\n"));
            assertTrue(logs.contains("event=login-failure"));
            assertTrue(logs.contains("ip=10.1.0.8"));
            assertTrue(logs.contains("ua=unknown"));
        } finally {
            logger.detachAppender(appender);
        }
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

    private AdminLoginReqDTO loginReq(String username, String password) {
        return AdminLoginReqDTO.builder()
                .username(username)
                .password(password)
                .build();
    }
}
