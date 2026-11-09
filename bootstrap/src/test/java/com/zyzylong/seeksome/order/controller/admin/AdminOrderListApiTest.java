package com.zyzylong.seeksome.order.controller.admin;

import cn.hutool.crypto.digest.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyzylong.seeksome.auth.dao.entity.AdminUserDO;
import com.zyzylong.seeksome.auth.dao.mapper.AdminUserMapper;
import com.zyzylong.seeksome.order.dao.entity.AuctionOrderDO;
import com.zyzylong.seeksome.order.dao.mapper.AuctionOrderMapper;
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
class AdminOrderListApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private AuctionOrderMapper auctionOrderMapper;

    @Test
    void list_requires_admin_login() throws Exception {
        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void list_returns_orders_sorted_by_create_time_desc() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        saveOrder(2001L, 11L, 21L, new BigDecimal("1200.00"), "paid", now.minusHours(2));
        AuctionOrderDO newest = saveOrder(2002L, 12L, 22L, new BigDecimal("3520.00"), "completed",
                now.minusHours(1));
        String token = loginAdmin();

        mockMvc.perform(get("/admin/orders").header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].orderId").value(newest.getId()))
                .andExpect(jsonPath("$.data.list[0].auctionId").value(2002))
                .andExpect(jsonPath("$.data.list[0].buyerId").value(12))
                .andExpect(jsonPath("$.data.list[0].sellerId").value(22))
                .andExpect(jsonPath("$.data.list[0].finalPrice").value(3520.00))
                .andExpect(jsonPath("$.data.list[0].status").value("completed"))
                .andExpect(jsonPath("$.data.list[0].payDeadline").exists())
                .andExpect(jsonPath("$.data.list[0].createTime").exists())
                .andExpect(jsonPath("$.data.list[1].auctionId").value(2001));
    }

    @Test
    void list_paginates_with_page_num_and_page_size() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        saveOrder(2101L, 11L, 21L, new BigDecimal("100.00"), "paid", now.minusHours(3));
        saveOrder(2102L, 11L, 21L, new BigDecimal("200.00"), "paid", now.minusHours(2));
        saveOrder(2103L, 11L, 21L, new BigDecimal("300.00"), "paid", now.minusHours(1));
        String token = loginAdmin();

        mockMvc.perform(get("/admin/orders")
                        .header("admin-token", token)
                        .param("pageNum", "2")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].auctionId").value(2101));
    }

    @Test
    void list_filters_by_status() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        saveOrder(2201L, 11L, 21L, new BigDecimal("100.00"), "paid", now.minusHours(2));
        AuctionOrderDO defaulted = saveOrder(2202L, 11L, 21L, new BigDecimal("200.00"), "defaulted",
                now.minusHours(1));
        String token = loginAdmin();

        mockMvc.perform(get("/admin/orders").header("admin-token", token).param("status", "defaulted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value(defaulted.getId()))
                .andExpect(jsonPath("$.data.list[0].status").value("defaulted"));
    }

    @Test
    void list_rejects_unknown_status() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(get("/admin/orders").header("admin-token", token).param("status", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void list_rejects_out_of_range_page_params() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(get("/admin/orders").header("admin-token", token).param("pageNum", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        mockMvc.perform(get("/admin/orders").header("admin-token", token).param("pageSize", "101"))
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

    private String loginAdmin() throws Exception {
        adminUserMapper.insert(AdminUserDO.builder()
                .username("admin_order_list")
                .password(BCrypt.hashpw("Passw0rd!", BCrypt.gensalt()))
                .role("ADMIN")
                .enabled(1)
                .failedCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build());
        String body = objectMapper.writeValueAsString(
                Map.of("username", "admin_order_list", "password", "Passw0rd!"));
        MvcResult result = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").path("token").asText();
    }
}
