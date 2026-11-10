package com.zyzylong.seeksome.wallet.controller.admin;

import cn.hutool.crypto.digest.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyzylong.seeksome.auth.dao.entity.AdminUserDO;
import com.zyzylong.seeksome.auth.dao.mapper.AdminUserMapper;
import com.zyzylong.seeksome.wallet.dao.entity.DepositLogDO;
import com.zyzylong.seeksome.wallet.dao.mapper.DepositLogMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminDepositLogListApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private DepositLogMapper depositLogMapper;

    @Test
    void list_requires_admin_login() throws Exception {
        mockMvc.perform(get("/admin/deposit-logs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void list_returns_all_types_sorted_by_create_time_desc() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        DepositLogDO freeze = saveDepositLog(41L, 4001L, new BigDecimal("10.00"), "freeze",
                now.minusHours(2));
        saveDepositLog(41L, 4001L, new BigDecimal("10.00"), "unfreeze", now.minusHours(1));
        DepositLogDO deduct = saveDepositLog(42L, 4002L, new BigDecimal("10.00"), "deduct",
                now.minusMinutes(10));
        String token = loginAdmin();

        mockMvc.perform(get("/admin/deposit-logs").header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list.length()").value(3))
                .andExpect(jsonPath("$.data.list[0].depositLogId").value(deduct.getId()))
                .andExpect(jsonPath("$.data.list[0].userId").value(42))
                .andExpect(jsonPath("$.data.list[0].auctionId").value(4002))
                .andExpect(jsonPath("$.data.list[0].amount").value(10.00))
                .andExpect(jsonPath("$.data.list[0].type").value("deduct"))
                .andExpect(jsonPath("$.data.list[0].createTime").exists())
                .andExpect(jsonPath("$.data.list[2].depositLogId").value(freeze.getId()))
                .andExpect(jsonPath("$.data.list[2].type").value("freeze"));
    }

    @Test
    void list_filters_by_type() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        saveDepositLog(51L, 5001L, new BigDecimal("10.00"), "freeze", now.minusHours(1));
        DepositLogDO deduct = saveDepositLog(51L, 5001L, new BigDecimal("10.00"), "deduct", now);
        String token = loginAdmin();

        mockMvc.perform(get("/admin/deposit-logs").header("admin-token", token).param("type", "deduct"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].depositLogId").value(deduct.getId()))
                .andExpect(jsonPath("$.data.list[0].type").value("deduct"));
    }

    @Test
    void list_rejects_unknown_type() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(get("/admin/deposit-logs").header("admin-token", token).param("type", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"))
                .andExpect(jsonPath("$.message").value("保证金流水类型不合法"));
    }

    @Test
    void list_paginates_logs() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        DepositLogDO oldest = saveDepositLog(61L, 6001L, new BigDecimal("10.00"), "freeze",
                now.minusHours(2));
        saveDepositLog(61L, 6001L, new BigDecimal("10.00"), "unfreeze", now.minusHours(1));
        saveDepositLog(61L, 6001L, new BigDecimal("10.00"), "deduct", now);
        String token = loginAdmin();

        mockMvc.perform(get("/admin/deposit-logs")
                        .header("admin-token", token)
                        .param("pageNum", "2")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].depositLogId").value(oldest.getId()));
    }

    @Test
    void list_rejects_out_of_range_page_params() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(get("/admin/deposit-logs").header("admin-token", token).param("pageNum", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        mockMvc.perform(get("/admin/deposit-logs").header("admin-token", token).param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    private DepositLogDO saveDepositLog(long userId, long auctionId, BigDecimal amount, String type,
                                        LocalDateTime createTime) {
        DepositLogDO depositLog = DepositLogDO.builder()
                .userId(userId)
                .auctionId(auctionId)
                .amount(amount)
                .type(type)
                .createTime(createTime)
                .updateTime(createTime)
                .build();
        depositLogMapper.insert(depositLog);
        return depositLog;
    }

    private String loginAdmin() throws Exception {
        adminUserMapper.insert(AdminUserDO.builder()
                .username("admin_deposit_log")
                .password(BCrypt.hashpw("Passw0rd!", BCrypt.gensalt()))
                .role("ADMIN")
                .enabled(1)
                .failedCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build());
        String body = objectMapper.writeValueAsString(
                Map.of("username", "admin_deposit_log", "password", "Passw0rd!"));
        MvcResult result = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").path("token").asText();
    }
}
