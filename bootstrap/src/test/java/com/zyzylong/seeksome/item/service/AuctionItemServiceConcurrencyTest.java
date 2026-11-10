package com.zyzylong.seeksome.item.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zyzylong.seeksome.item.controller.admin.request.AdminItemRejectRequest;
import com.zyzylong.seeksome.item.dao.entity.AuctionItemDO;
import com.zyzylong.seeksome.item.dao.mapper.AuctionItemMapper;
import com.zyzylong.seeksome.item.enums.ItemErrorCodeEnum;
import com.zyzylong.seeksome.item.exception.ItemException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuctionItemServiceConcurrencyTest {

    @Autowired
    private AuctionItemService auctionItemService;

    @MockitoSpyBean
    private AuctionItemMapper auctionItemMapper;

    @Test
    void updateItemApproved_rejects_when_status_changed_after_read() {
        AuctionItemDO item = saveItem("pending_review");
        doReturn(item).when(auctionItemMapper).selectById(item.getId());
        updateStatus(item.getId(), "cancelled");

        ItemException exception = assertThrows(ItemException.class,
                () -> auctionItemService.updateItemApproved(item.getId()));

        assertEquals(ItemErrorCodeEnum.ITEM_STATUS_NOT_ALLOWED, exception.getErrorCode());
        assertEquals("cancelled", selectItem(item.getId()).getStatus());
    }

    @Test
    void updateItemRejected_rejects_when_status_changed_after_read() {
        AuctionItemDO item = saveItem("pending_review");
        doReturn(item).when(auctionItemMapper).selectById(item.getId());
        updateStatus(item.getId(), "cancelled");

        ItemException exception = assertThrows(ItemException.class,
                () -> auctionItemService.updateItemRejected(item.getId(),
                        AdminItemRejectRequest.builder().rejectReason("描述不符").build()));

        assertEquals(ItemErrorCodeEnum.ITEM_STATUS_NOT_ALLOWED, exception.getErrorCode());
        AuctionItemDO saved = selectItem(item.getId());
        assertEquals("cancelled", saved.getStatus());
        assertNull(saved.getRejectReason());
    }

    @Test
    void updateItemCancelled_rejects_when_status_changed_after_read() {
        AuctionItemDO item = saveItem("pending_review");
        doReturn(item).when(auctionItemMapper).selectById(item.getId());
        updateStatus(item.getId(), "ended");

        ItemException exception = assertThrows(ItemException.class,
                () -> auctionItemService.updateItemCancelled(item.getId()));

        assertEquals(ItemErrorCodeEnum.ITEM_STATUS_NOT_ALLOWED, exception.getErrorCode());
        assertEquals("ended", selectItem(item.getId()).getStatus());
    }

    private AuctionItemDO saveItem(String status) {
        LocalDateTime now = LocalDateTime.now();
        AuctionItemDO item = AuctionItemDO.builder()
                .sellerId(11L)
                .title("并发测试拍品")
                .description("并发测试")
                .categoryId(21L)
                .conditionLevel("90_new")
                .images("https://example.com/item.jpg")
                .startPrice(new BigDecimal("100.00"))
                .minIncrement(new BigDecimal("10.00"))
                .durationHours(24)
                .status(status)
                .createTime(now)
                .updateTime(now)
                .build();
        auctionItemMapper.insert(item);
        return item;
    }

    private void updateStatus(Long itemId, String status) {
        auctionItemMapper.update(null, new LambdaUpdateWrapper<AuctionItemDO>()
                .eq(AuctionItemDO::getId, itemId)
                .set(AuctionItemDO::getStatus, status));
    }

    private AuctionItemDO selectItem(Long itemId) {
        return auctionItemMapper.selectOne(new LambdaQueryWrapper<AuctionItemDO>()
                .eq(AuctionItemDO::getId, itemId));
    }
}
