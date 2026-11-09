package com.zyzylong.seeksome.item.controller.admin;

import cn.hutool.crypto.digest.BCrypt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyzylong.seeksome.auth.dao.entity.AdminUserDO;
import com.zyzylong.seeksome.auth.dao.mapper.AdminUserMapper;
import com.zyzylong.seeksome.item.dao.entity.AuctionItemDO;
import com.zyzylong.seeksome.item.dao.mapper.AuctionItemMapper;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
class AdminItemReviewApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private AuctionItemMapper auctionItemMapper;

    @Test
    void api_doc_exposes_flat_query_parameters_for_item_list() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/admin/items'].get.parameters[*].name",
                        hasItems("pageNum", "pageSize", "status", "categoryId", "sellerId")));
    }

    @Test
    void list_requires_admin_login() throws Exception {
        mockMvc.perform(get("/admin/items"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void list_returns_all_items_ordered_by_create_time_desc_when_status_absent() throws Exception {
        LocalDateTime base = LocalDateTime.now();
        AuctionItemDO pending = saveItem(11L, 21L, "旧待审核拍品", "pending_review", 48, base.minusHours(3));
        AuctionItemDO active = saveItem(12L, 22L, "在拍拍品", "active", 24, base.minusHours(2));
        active.setCurrentPrice(new BigDecimal("75.00"));
        active.setStartTime(base.minusHours(1));
        active.setEndTime(base.plusHours(23));
        auctionItemMapper.updateById(active);
        AuctionItemDO rejected = saveItem(13L, 23L, "驳回拍品", "rejected", 24, base.minusHours(1));
        rejected.setRejectReason("描述不符");
        auctionItemMapper.updateById(rejected);
        String token = loginAdmin();

        mockMvc.perform(get("/admin/items").header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.list.length()").value(3))
                .andExpect(jsonPath("$.data.list[0].itemId").value(rejected.getId()))
                .andExpect(jsonPath("$.data.list[0].status").value("rejected"))
                .andExpect(jsonPath("$.data.list[0].rejectReason").value("描述不符"))
                .andExpect(jsonPath("$.data.list[1].itemId").value(active.getId()))
                .andExpect(jsonPath("$.data.list[1].status").value("active"))
                .andExpect(jsonPath("$.data.list[1].currentPrice").value(75.00))
                .andExpect(jsonPath("$.data.list[1].startTime").exists())
                .andExpect(jsonPath("$.data.list[1].endTime").exists())
                .andExpect(jsonPath("$.data.list[2].itemId").value(pending.getId()))
                .andExpect(jsonPath("$.data.list[2].status").value("pending_review"));
    }

    @Test
    void list_filters_by_status() throws Exception {
        LocalDateTime base = LocalDateTime.now();
        AuctionItemDO older = saveItem(11L, 21L, "旧待审核拍品", "pending_review", 48, base.minusHours(2));
        AuctionItemDO newer = saveItem(12L, 22L, "新待审核拍品", "pending_review", 24, base.minusHours(1));
        saveItem(11L, 21L, "在拍拍品", "active", 48, base);
        String token = loginAdmin();

        mockMvc.perform(get("/admin/items")
                        .header("admin-token", token)
                        .param("status", "pending_review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].itemId").value(newer.getId()))
                .andExpect(jsonPath("$.data.list[0].sellerId").value(12))
                .andExpect(jsonPath("$.data.list[0].title").value("新待审核拍品"))
                .andExpect(jsonPath("$.data.list[0].description").value("描述-新待审核拍品"))
                .andExpect(jsonPath("$.data.list[0].categoryId").value(22))
                .andExpect(jsonPath("$.data.list[0].conditionLevel").value("90_new"))
                .andExpect(jsonPath("$.data.list[0].images").value("https://example.com/新待审核拍品.jpg"))
                .andExpect(jsonPath("$.data.list[0].startPrice").value(100.00))
                .andExpect(jsonPath("$.data.list[0].minIncrement").value(10.00))
                .andExpect(jsonPath("$.data.list[0].durationHours").value(24))
                .andExpect(jsonPath("$.data.list[0].status").value("pending_review"))
                .andExpect(jsonPath("$.data.list[0].createTime").exists())
                .andExpect(jsonPath("$.data.list[1].itemId").value(older.getId()));
    }

    @Test
    void list_paginates_with_page_num_and_page_size() throws Exception {
        LocalDateTime base = LocalDateTime.now();
        AuctionItemDO oldest = saveItem(11L, 21L, "待审核一", "pending_review", 48, base.minusHours(3));
        saveItem(11L, 21L, "待审核二", "pending_review", 48, base.minusHours(2));
        saveItem(11L, 21L, "待审核三", "pending_review", 48, base.minusHours(1));
        String token = loginAdmin();

        mockMvc.perform(get("/admin/items")
                        .header("admin-token", token)
                        .param("pageNum", "2")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.pageNum").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].itemId").value(oldest.getId()));
    }

    @Test
    void list_filters_by_category_across_statuses() throws Exception {
        saveItem(11L, 21L, "数码待审核", "pending_review", 48, LocalDateTime.now());
        saveItem(11L, 22L, "穿搭在拍", "active", 48, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(get("/admin/items")
                        .header("admin-token", token)
                        .param("categoryId", "22"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].title").value("穿搭在拍"));
    }

    @Test
    void list_filters_by_seller_across_statuses() throws Exception {
        saveItem(11L, 21L, "卖家一的拍品", "pending_review", 48, LocalDateTime.now());
        saveItem(12L, 21L, "卖家二的拍品", "active", 48, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(get("/admin/items")
                        .header("admin-token", token)
                        .param("sellerId", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].title").value("卖家二的拍品"));
    }

    @Test
    void list_rejects_unknown_status() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(get("/admin/items")
                        .header("admin-token", token)
                        .param("status", "unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void list_rejects_out_of_range_page_params() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(get("/admin/items")
                        .header("admin-token", token)
                        .param("pageNum", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
        mockMvc.perform(get("/admin/items")
                        .header("admin-token", token)
                        .param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void approve_requires_admin_login() throws Exception {
        mockMvc.perform(post("/admin/items/{itemId}/approve", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void approve_activates_pending_item_and_sets_auction_window() throws Exception {
        AuctionItemDO item = saveItem(11L, 21L, "待审核拍品", "pending_review", 24, LocalDateTime.now());
        String token = loginAdmin();
        LocalDateTime before = LocalDateTime.now();

        mockMvc.perform(post("/admin/items/{itemId}/approve", item.getId())
                        .header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        AuctionItemDO approved = auctionItemMapper.selectById(item.getId());
        assertEquals("active", approved.getStatus());
        assertNotNull(approved.getStartTime());
        assertNotNull(approved.getEndTime());
        assertEquals(24, Duration.between(approved.getStartTime(), approved.getEndTime()).toHours());
        assertFalse(approved.getStartTime().isBefore(before.minusSeconds(5)));
        assertTrue(approved.getStartTime().isBefore(LocalDateTime.now().plusSeconds(5)));
    }

    @Test
    void approve_unknown_item_returns_404() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/admin/items/{itemId}/approve", 999999L)
                        .header("admin-token", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A0404"));
    }

    @Test
    void approve_rejects_item_not_in_pending_review_status() throws Exception {
        AuctionItemDO active = saveItem(11L, 21L, "已在拍", "active", 24, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(post("/admin/items/{itemId}/approve", active.getId())
                        .header("admin-token", token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A0502"));

        AuctionItemDO unchanged = auctionItemMapper.selectById(active.getId());
        assertEquals("active", unchanged.getStatus());
        assertNull(unchanged.getStartTime());
    }

    @Test
    void reject_requires_admin_login() throws Exception {
        mockMvc.perform(post("/admin/items/{itemId}/reject", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rejectReason", "描述不符"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void reject_marks_pending_item_rejected_with_trimmed_reason() throws Exception {
        AuctionItemDO item = saveItem(11L, 21L, "待审核拍品", "pending_review", 24, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(post("/admin/items/{itemId}/reject", item.getId())
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("rejectReason", "  描述与实际不符  "))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        AuctionItemDO rejected = auctionItemMapper.selectById(item.getId());
        assertEquals("rejected", rejected.getStatus());
        assertEquals("描述与实际不符", rejected.getRejectReason());
        assertNull(rejected.getStartTime());
        assertNull(rejected.getEndTime());
    }

    @Test
    void reject_rejects_blank_reason() throws Exception {
        AuctionItemDO item = saveItem(11L, 21L, "待审核拍品", "pending_review", 24, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(post("/admin/items/{itemId}/reject", item.getId())
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rejectReason", "   "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void reject_rejects_reason_longer_than_255() throws Exception {
        AuctionItemDO item = saveItem(11L, 21L, "待审核拍品", "pending_review", 24, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(post("/admin/items/{itemId}/reject", item.getId())
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rejectReason", "a".repeat(256)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void reject_unknown_item_returns_404() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/admin/items/{itemId}/reject", 999999L)
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rejectReason", "描述不符"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A0404"));
    }

    @Test
    void reject_rejects_item_not_in_pending_review_status() throws Exception {
        AuctionItemDO active = saveItem(11L, 21L, "已在拍", "active", 24, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(post("/admin/items/{itemId}/reject", active.getId())
                        .header("admin-token", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rejectReason", "描述不符"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A0502"));

        AuctionItemDO unchanged = auctionItemMapper.selectById(active.getId());
        assertEquals("active", unchanged.getStatus());
        assertNull(unchanged.getRejectReason());
    }

    @Test
    void cancel_requires_admin_login() throws Exception {
        mockMvc.perform(post("/admin/items/{itemId}/cancel", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void cancel_pending_item_marks_cancelled() throws Exception {
        AuctionItemDO item = saveItem(11L, 21L, "待审核拍品", "pending_review", 24, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(post("/admin/items/{itemId}/cancel", item.getId())
                        .header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        assertEquals("cancelled", auctionItemMapper.selectById(item.getId()).getStatus());
    }

    @Test
    void cancel_active_item_marks_cancelled() throws Exception {
        AuctionItemDO item = saveItem(11L, 21L, "在拍拍品", "active", 24, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(post("/admin/items/{itemId}/cancel", item.getId())
                        .header("admin-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        assertEquals("cancelled", auctionItemMapper.selectById(item.getId()).getStatus());
    }

    @Test
    void cancel_rejects_item_in_terminal_status() throws Exception {
        AuctionItemDO ended = saveItem(11L, 21L, "已结束", "ended", 24, LocalDateTime.now());
        AuctionItemDO rejected = saveItem(11L, 21L, "已驳回", "rejected", 24, LocalDateTime.now());
        String token = loginAdmin();

        mockMvc.perform(post("/admin/items/{itemId}/cancel", ended.getId())
                        .header("admin-token", token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A0502"));
        mockMvc.perform(post("/admin/items/{itemId}/cancel", rejected.getId())
                        .header("admin-token", token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("A0502"));

        assertEquals("ended", auctionItemMapper.selectById(ended.getId()).getStatus());
        assertEquals("rejected", auctionItemMapper.selectById(rejected.getId()).getStatus());
    }

    @Test
    void cancel_unknown_item_returns_404() throws Exception {
        String token = loginAdmin();

        mockMvc.perform(post("/admin/items/{itemId}/cancel", 999999L)
                        .header("admin-token", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("A0404"));
    }

    private AuctionItemDO saveItem(Long sellerId, Long categoryId, String title, String status,
                                   int durationHours, LocalDateTime createTime) {
        AuctionItemDO item = AuctionItemDO.builder()
                .sellerId(sellerId)
                .title(title)
                .description("描述-" + title)
                .categoryId(categoryId)
                .conditionLevel("90_new")
                .images("https://example.com/" + title + ".jpg")
                .startPrice(new BigDecimal("100.00"))
                .minIncrement(new BigDecimal("10.00"))
                .durationHours(durationHours)
                .status(status)
                .createTime(createTime)
                .updateTime(createTime)
                .build();
        auctionItemMapper.insert(item);
        return item;
    }

    private String loginAdmin() throws Exception {
        adminUserMapper.insert(AdminUserDO.builder()
                .username("admin_item_review")
                .password(BCrypt.hashpw("Passw0rd!", BCrypt.gensalt()))
                .role("ADMIN")
                .enabled(1)
                .failedCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build());
        String body = objectMapper.writeValueAsString(
                Map.of("username", "admin_item_review", "password", "Passw0rd!"));
        MvcResult result = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .path("data").path("token").asText();
    }
}
