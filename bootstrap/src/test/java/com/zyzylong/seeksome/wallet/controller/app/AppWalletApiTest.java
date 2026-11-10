package com.zyzylong.seeksome.wallet.controller.app;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyzylong.seeksome.user.dao.entity.UserDO;
import com.zyzylong.seeksome.user.dao.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppWalletApiTest {

    private static final String PASSWORD_HASH = BCrypt.hashpw("123456", BCrypt.gensalt());

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void getWallet_requires_login() throws Exception {
        mockMvc.perform(get("/app/wallets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void getWallet_returns_current_user_balance_and_frozen_balance() throws Exception {
        saveUser("wallet_other", new BigDecimal("500.00"), new BigDecimal("80.00"));
        UserDO user = saveUser("wallet_user", new BigDecimal("120.50"), new BigDecimal("10.00"));
        String token = login(user.getId());

        mockMvc.perform(get("/app/wallets").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.walletBalance").value(120.50))
                .andExpect(jsonPath("$.data.frozenBalance").value(10.00));
    }

    @Test
    void getWallet_rejects_frozen_user() throws Exception {
        UserDO user = saveUser("wallet_frozen_user", new BigDecimal("10.00"), BigDecimal.ZERO);
        String token = login(user.getId());
        updateStatus(user.getId(), "frozen");

        mockMvc.perform(get("/app/wallets").header("satoken", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("A0301"));
    }

    @Test
    void recharge_requires_login() throws Exception {
        postRecharge(null, rechargeBody(new BigDecimal("10.00")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void recharge_credits_balance_and_writes_recharge_log() throws Exception {
        UserDO user = saveUser("recharge_user", new BigDecimal("100.00"), new BigDecimal("5.00"));
        String token = login(user.getId());

        postRecharge(token, rechargeBody(new BigDecimal("50.25")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.walletBalance").value(150.25))
                .andExpect(jsonPath("$.data.frozenBalance").value(5.00));

        UserDO updated = userMapper.selectById(user.getId());
        assertEquals(0, updated.getWalletBalance().compareTo(new BigDecimal("150.25")));

        Map<String, Object> walletLog = jdbcTemplate.queryForMap(
                "SELECT user_id, amount, type, related_type, related_id FROM wallet_log WHERE user_id = ?",
                user.getId());
        assertEquals(user.getId().longValue(), ((Number) walletLog.get("user_id")).longValue());
        assertEquals(0, ((BigDecimal) walletLog.get("amount")).compareTo(new BigDecimal("50.25")));
        assertEquals("recharge", walletLog.get("type"));
        assertNull(walletLog.get("related_type"));
        assertNull(walletLog.get("related_id"));
    }

    @Test
    void recharge_accepts_min_and_max_boundary_amounts() throws Exception {
        UserDO user = saveUser("recharge_boundary_user", BigDecimal.ZERO, BigDecimal.ZERO);
        String token = login(user.getId());

        postRecharge(token, rechargeBody(new BigDecimal("10000.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.walletBalance").value(10000.00));
        postRecharge(token, rechargeBody(new BigDecimal("0.01")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.walletBalance").value(10000.01));
        assertEquals(2, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wallet_log WHERE user_id = ? AND type = 'recharge'",
                Integer.class, user.getId()));
    }

    @Test
    void recharge_rejects_invalid_amounts() throws Exception {
        UserDO user = saveUser("recharge_invalid_user", new BigDecimal("100.00"), BigDecimal.ZERO);
        String token = login(user.getId());

        postRecharge(token, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        postRecharge(token, "{\"amount\":0}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        postRecharge(token, "{\"amount\":-1}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        postRecharge(token, "{\"amount\":0.001}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        postRecharge(token, "{\"amount\":10000.01}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));

        UserDO unchanged = userMapper.selectById(user.getId());
        assertEquals(0, unchanged.getWalletBalance().compareTo(new BigDecimal("100.00")));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wallet_log WHERE user_id = ?", Integer.class, user.getId()));
    }

    @Test
    void recharge_rejects_frozen_user() throws Exception {
        UserDO user = saveUser("recharge_frozen_user", new BigDecimal("100.00"), BigDecimal.ZERO);
        String token = login(user.getId());
        updateStatus(user.getId(), "frozen");

        postRecharge(token, rechargeBody(new BigDecimal("10.00")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("A0301"));

        UserDO unchanged = userMapper.selectById(user.getId());
        assertEquals(0, unchanged.getWalletBalance().compareTo(new BigDecimal("100.00")));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wallet_log WHERE user_id = ?", Integer.class, user.getId()));
    }

    @Test
    void listWalletLogs_requires_login() throws Exception {
        mockMvc.perform(get("/app/wallets/logs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void listWalletLogs_returns_current_user_logs_newest_first() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        UserDO user = saveUser("wallet_log_user", BigDecimal.ZERO, BigDecimal.ZERO);
        UserDO other = saveUser("wallet_log_other", BigDecimal.ZERO, BigDecimal.ZERO);
        insertWalletLog(other.getId(), new BigDecimal("99.00"), "recharge", null, null,
                now.minusMinutes(5));
        insertWalletLog(user.getId(), new BigDecimal("20.00"), "recharge", null, null,
                now.minusHours(2));
        insertWalletLog(user.getId(), new BigDecimal("10.00"), "freeze", "auction", 7001L,
                now.minusHours(1));
        String token = login(user.getId());

        mockMvc.perform(get("/app/wallets/logs").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].walletLogId").exists())
                .andExpect(jsonPath("$.data.list[0].amount").value(10.00))
                .andExpect(jsonPath("$.data.list[0].type").value("freeze"))
                .andExpect(jsonPath("$.data.list[0].relatedType").value("auction"))
                .andExpect(jsonPath("$.data.list[0].relatedId").value(7001))
                .andExpect(jsonPath("$.data.list[0].createTime").exists())
                .andExpect(jsonPath("$.data.list[1].amount").value(20.00))
                .andExpect(jsonPath("$.data.list[1].type").value("recharge"));
    }

    @Test
    void listWalletLogs_uses_default_pagination() throws Exception {
        UserDO user = saveUser("wallet_log_page_user", BigDecimal.ZERO, BigDecimal.ZERO);
        LocalDateTime now = LocalDateTime.now();
        for (int each = 0; each < 12; each++) {
            insertWalletLog(user.getId(), new BigDecimal(each + 1), "recharge", null, null,
                    now.minusMinutes(20 - each));
        }
        String token = login(user.getId());

        mockMvc.perform(get("/app/wallets/logs").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(12))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list.length()").value(10))
                .andExpect(jsonPath("$.data.list[0].amount").value(12.00));
    }

    @Test
    void listWalletLogs_paginates_with_page_num_and_page_size() throws Exception {
        UserDO user = saveUser("wallet_log_page2_user", BigDecimal.ZERO, BigDecimal.ZERO);
        LocalDateTime now = LocalDateTime.now();
        insertWalletLog(user.getId(), new BigDecimal("1.00"), "recharge", null, null,
                now.minusHours(2));
        insertWalletLog(user.getId(), new BigDecimal("2.00"), "freeze", "auction", 8001L,
                now.minusHours(1));
        insertWalletLog(user.getId(), new BigDecimal("3.00"), "unfreeze", "auction", 8001L,
                now.minusMinutes(1));
        String token = login(user.getId());

        mockMvc.perform(get("/app/wallets/logs")
                        .header("satoken", token)
                        .param("pageNum", "2")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].amount").value(1.00));
    }

    @Test
    void listWalletLogs_returns_empty_page_when_user_has_no_logs() throws Exception {
        UserDO user = saveUser("wallet_log_empty_user", BigDecimal.ZERO, BigDecimal.ZERO);
        String token = login(user.getId());

        mockMvc.perform(get("/app/wallets/logs").header("satoken", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    @Test
    void listWalletLogs_rejects_out_of_range_page_params() throws Exception {
        UserDO user = saveUser("wallet_log_invalid_user", BigDecimal.ZERO, BigDecimal.ZERO);
        String token = login(user.getId());

        mockMvc.perform(get("/app/wallets/logs").header("satoken", token).param("pageNum", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        mockMvc.perform(get("/app/wallets/logs").header("satoken", token).param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        mockMvc.perform(get("/app/wallets/logs").header("satoken", token).param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void listWalletLogs_rejects_frozen_user() throws Exception {
        UserDO user = saveUser("wallet_log_frozen_user", BigDecimal.ZERO, BigDecimal.ZERO);
        String token = login(user.getId());
        updateStatus(user.getId(), "frozen");

        mockMvc.perform(get("/app/wallets/logs").header("satoken", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("A0301"));
    }

    private ResultActions postRecharge(String token, String body) throws Exception {
        var request = post("/app/wallets/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
        if (token != null) {
            request.header("satoken", token);
        }
        return mockMvc.perform(request);
    }

    private String rechargeBody(BigDecimal amount) throws Exception {
        return objectMapper.writeValueAsString(Map.of("amount", amount));
    }

    private UserDO saveUser(String username, BigDecimal walletBalance, BigDecimal frozenBalance) {
        LocalDateTime now = LocalDateTime.now();
        UserDO user = UserDO.builder()
                .username(username)
                .password(PASSWORD_HASH)
                .nickname("昵称_" + username)
                .creditScore(100)
                .walletBalance(walletBalance)
                .frozenBalance(frozenBalance)
                .status("normal")
                .createTime(now)
                .updateTime(now)
                .build();
        userMapper.insert(user);
        return user;
    }

    private void updateStatus(Long userId, String status) {
        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId)
                .set(UserDO::getStatus, status));
    }

    private void insertWalletLog(Long userId, BigDecimal amount, String type, String relatedType,
                                 Long relatedId, LocalDateTime createTime) {
        jdbcTemplate.update("INSERT INTO wallet_log "
                        + "(user_id, amount, type, related_type, related_id, create_time, update_time) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                userId, amount, type, relatedType, relatedId, createTime, createTime);
    }

    private String login(Long userId) throws Exception {
        MvcResult result = mockMvc.perform(get("/test/user/login").param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getHeader("satoken");
    }
}
