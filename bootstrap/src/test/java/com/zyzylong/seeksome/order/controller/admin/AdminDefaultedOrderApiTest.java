package com.zyzylong.seeksome.order.controller.admin;

import cn.hutool.crypto.digest.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyzylong.seeksome.auth.dao.entity.AdminUserDO;
import com.zyzylong.seeksome.auth.dao.mapper.AdminUserMapper;
import com.zyzylong.seeksome.order.dao.entity.AuctionOrderDO;
import com.zyzylong.seeksome.order.dao.mapper.AuctionOrderMapper;
import com.zyzylong.seeksome.user.dao.entity.CreditLogDO;
import com.zyzylong.seeksome.user.dao.mapper.CreditLogMapper;
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

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminDefaultedOrderApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private AuctionOrderMapper auctionOrderMapper;

    @Autowired
    private DepositLogMapper depositLogMapper;

    @Autowired
    private CreditLogMapper creditLogMapper;

    @Test
    void list_requires_admin_login() throws Exception {
        mockMvc.perform(get("/admin/orders/defaulted"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void list_returns_only_defaulted_orders_with_penalty_detail() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        AuctionOrderDO defaulted = saveOrder(3001L, 31L, 41L, new BigDecimal("2600.00"), "defaulted",
                now.minusHours(2));
        saveOrder(3002L, 32L, 42L, new BigDecimal("990.00"), "paid", now.minusHours(1));
        saveDepositLog(31L, 3001L, new BigDecimal("10.00"), now.minusMinutes(20));
        saveCreditLog(31L, defaulted.getId(), -20, "超时未支付，违约扣分", now.minusMinutes(20));
        String token = loginAdmin();

        mockMvc.perform(get("/admin/orders/defaulted").header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value(defaulted.getId()))
                .andExpect(jsonPath("$.data.list[0].auctionId").value(3001))
                .andExpect(jsonPath("$.data.list[0].buyerId").value(31))
                .andExpect(jsonPath("$.data.list[0].sellerId").value(41))
                .andExpect(jsonPath("$.data.list[0].finalPrice").value(2600.00))
                .andExpect(jsonPath("$.data.list[0].payDeadline").exists())
                .andExpect(jsonPath("$.data.list[0].createTime").exists())
                .andExpect(jsonPath("$.data.list[0].depositDeductAmount").value(10.00))
                .andExpect(jsonPath("$.data.list[0].creditChange").value(-20))
                .andExpect(jsonPath("$.data.list[0].creditReason").value("超时未支付，违约扣分"))
                .andExpect(jsonPath("$.data.list[0].penaltyTime").exists());
    }

    @Test
    void list_keeps_penalty_fields_null_when_no_logs() throws Exception {
        AuctionOrderDO defaulted = saveOrder(3101L, 31L, 41L, new BigDecimal("500.00"), "defaulted",
                LocalDateTime.now().minusHours(1));
        String token = loginAdmin();

        mockMvc.perform(get("/admin/orders/defaulted").header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value(defaulted.getId()))
                .andExpect(jsonPath("$.data.list[0].depositDeductAmount").value(nullValue()))
                .andExpect(jsonPath("$.data.list[0].creditChange").value(nullValue()))
                .andExpect(jsonPath("$.data.list[0].creditReason").value(nullValue()))
                .andExpect(jsonPath("$.data.list[0].penaltyTime").value(nullValue()));
    }

    @Test
    void list_ignores_credit_logs_of_other_users() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        AuctionOrderDO defaulted = saveOrder(3301L, 31L, 41L, new BigDecimal("800.00"), "defaulted",
                now.minusHours(2));
        saveCreditLog(31L, defaulted.getId(), -20, "超时未支付，违约扣分", now.minusMinutes(30));
        saveCreditLog(99L, defaulted.getId(), -50, "其他用户扣分", now.minusMinutes(10));
        String token = loginAdmin();

        mockMvc.perform(get("/admin/orders/defaulted").header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].creditChange").value(-20))
                .andExpect(jsonPath("$.data.list[0].creditReason").value("超时未支付，违约扣分"));
    }

    @Test
    void list_paginates_defaulted_orders() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        AuctionOrderDO older = saveOrder(3201L, 31L, 41L, new BigDecimal("100.00"), "defaulted",
                now.minusHours(2));
        saveOrder(3202L, 31L, 41L, new BigDecimal("200.00"), "defaulted", now.minusHours(1));
        String token = loginAdmin();

        mockMvc.perform(get("/admin/orders/defaulted")
                        .header("admin-token", token)
                        .param("pageNum", "2")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value(older.getId()));
    }

    @Test
    void list_rejects_out_of_range_page_params() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(get("/admin/orders/defaulted").header("admin-token", token).param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    private AuctionOrderDO saveOrder(long auctionId, long buyerId, long sellerId, BigDecimal finalPrice,
                                     String status, LocalDateTime createTime) {
        AuctionOrderDO order = AuctionOrderDO.builder()
                .auctionId(auctionId)
                .buyerId(buyerId)
                .sellerId(sellerId)
                .finalPrice(finalPrice)
                .status(status)
                .payDeadline(createTime.plusMinutes(30))
                .createTime(createTime)
                .updateTime(createTime)
                .build();
        auctionOrderMapper.insert(order);
        return order;
    }

    private void saveDepositLog(long userId, long auctionId, BigDecimal amount, LocalDateTime createTime) {
        depositLogMapper.insert(DepositLogDO.builder()
                .userId(userId)
                .auctionId(auctionId)
                .amount(amount)
                .type("deduct")
                .createTime(createTime)
                .updateTime(createTime)
                .build());
    }

    private void saveCreditLog(long userId, long orderId, int changeValue, String reason,
                               LocalDateTime createTime) {
        creditLogMapper.insert(CreditLogDO.builder()
                .userId(userId)
                .changeValue(changeValue)
                .reason(reason)
                .relatedType("order")
                .relatedId(orderId)
                .createTime(createTime)
                .updateTime(createTime)
                .build());
    }

    private String loginAdmin() throws Exception {
        adminUserMapper.insert(AdminUserDO.builder()
                .username("admin_defaulted_order")
                .password(BCrypt.hashpw("Passw0rd!", BCrypt.gensalt()))
                .role("ADMIN")
                .enabled(1)
                .failedCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build());
        String body = objectMapper.writeValueAsString(
                Map.of("username", "admin_defaulted_order", "password", "Passw0rd!"));
        MvcResult result = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").path("token").asText();
    }
}
