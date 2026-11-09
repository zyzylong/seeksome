package com.zyzylong.seeksome.item.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.item.controller.admin.request.AdminItemPageQueryRequest;
import com.zyzylong.seeksome.item.controller.admin.request.AdminItemRejectRequest;
import com.zyzylong.seeksome.item.controller.admin.vo.AdminItemVO;
import com.zyzylong.seeksome.item.dao.entity.AuctionItemDO;
import com.zyzylong.seeksome.item.dao.mapper.AuctionItemMapper;
import com.zyzylong.seeksome.item.enums.ItemErrorCodeEnum;
import com.zyzylong.seeksome.item.enums.ItemStatusEnum;
import com.zyzylong.seeksome.item.exception.ItemException;
import com.zyzylong.seeksome.item.service.AuctionItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 拍品管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class AuctionItemServiceImpl implements AuctionItemService {

    private final AuctionItemMapper auctionItemMapper;

    @Override
    public PageResult<AdminItemVO> listItems(AdminItemPageQueryRequest requestParam) {
        LambdaQueryWrapper<AuctionItemDO> queryWrapper = new LambdaQueryWrapper<>();
        String status = StrUtil.trimToNull(requestParam.getStatus());
        if (status != null) {
            boolean knownStatus = Arrays.stream(ItemStatusEnum.values())
                    .anyMatch(each -> each.getValue().equals(status));
            if (!knownStatus) {
                throw new ItemException(CommonErrorCodeEnum.PARAM_INVALID, "拍品状态不合法");
            }
            queryWrapper.eq(AuctionItemDO::getStatus, status);
        }
        if (requestParam.getCategoryId() != null) {
            queryWrapper.eq(AuctionItemDO::getCategoryId, requestParam.getCategoryId());
        }
        if (requestParam.getSellerId() != null) {
            queryWrapper.eq(AuctionItemDO::getSellerId, requestParam.getSellerId());
        }
        queryWrapper.orderByDesc(AuctionItemDO::getCreateTime).orderByDesc(AuctionItemDO::getId);

        Page<AuctionItemDO> page = auctionItemMapper.selectPage(
                Page.of(requestParam.getPageNum(), requestParam.getPageSize()), queryWrapper);
        List<AdminItemVO> list = page.getRecords().stream().map(this::toAdminItemVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    @Override
    public void updateItemApproved(Long itemId) {
        AuctionItemDO item = requireItem(itemId);
        if (!ItemStatusEnum.PENDING_REVIEW.getValue().equals(item.getStatus())) {
            throw new ItemException(ItemErrorCodeEnum.ITEM_STATUS_NOT_ALLOWED, "仅待审核拍品可审核通过");
        }
        LocalDateTime now = LocalDateTime.now();
        auctionItemMapper.update(null, new LambdaUpdateWrapper<AuctionItemDO>()
                .eq(AuctionItemDO::getId, itemId)
                .set(AuctionItemDO::getStatus, ItemStatusEnum.ACTIVE.getValue())
                .set(AuctionItemDO::getStartTime, now)
                .set(AuctionItemDO::getEndTime, now.plusHours(item.getDurationHours()))
                .set(AuctionItemDO::getUpdateTime, now));
    }

    @Override
    public void updateItemRejected(Long itemId, AdminItemRejectRequest requestParam) {
        AuctionItemDO item = requireItem(itemId);
        if (!ItemStatusEnum.PENDING_REVIEW.getValue().equals(item.getStatus())) {
            throw new ItemException(ItemErrorCodeEnum.ITEM_STATUS_NOT_ALLOWED, "仅待审核拍品可审核驳回");
        }
        auctionItemMapper.update(null, new LambdaUpdateWrapper<AuctionItemDO>()
                .eq(AuctionItemDO::getId, itemId)
                .set(AuctionItemDO::getStatus, ItemStatusEnum.REJECTED.getValue())
                .set(AuctionItemDO::getRejectReason, StrUtil.trim(requestParam.getRejectReason()))
                .set(AuctionItemDO::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public void updateItemCancelled(Long itemId) {
        AuctionItemDO item = requireItem(itemId);
        boolean cancellable = ItemStatusEnum.PENDING_REVIEW.getValue().equals(item.getStatus())
                || ItemStatusEnum.ACTIVE.getValue().equals(item.getStatus());
        if (!cancellable) {
            throw new ItemException(ItemErrorCodeEnum.ITEM_STATUS_NOT_ALLOWED, "仅待审核或在拍拍品可下架");
        }
        auctionItemMapper.update(null, new LambdaUpdateWrapper<AuctionItemDO>()
                .eq(AuctionItemDO::getId, itemId)
                .set(AuctionItemDO::getStatus, ItemStatusEnum.CANCELLED.getValue())
                .set(AuctionItemDO::getUpdateTime, LocalDateTime.now()));
    }

    private AuctionItemDO requireItem(Long itemId) {
        AuctionItemDO item = auctionItemMapper.selectById(itemId);
        if (item == null) {
            throw new ItemException(CommonErrorCodeEnum.NOT_FOUND);
        }
        return item;
    }

    private AdminItemVO toAdminItemVO(AuctionItemDO item) {
        return AdminItemVO.builder()
                .itemId(item.getId())
                .sellerId(item.getSellerId())
                .title(item.getTitle())
                .description(item.getDescription())
                .categoryId(item.getCategoryId())
                .conditionLevel(item.getConditionLevel())
                .images(item.getImages())
                .startPrice(item.getStartPrice())
                .minIncrement(item.getMinIncrement())
                .durationHours(item.getDurationHours())
                .currentPrice(item.getCurrentPrice())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .status(item.getStatus())
                .rejectReason(item.getRejectReason())
                .createTime(item.getCreateTime())
                .build();
    }
}
