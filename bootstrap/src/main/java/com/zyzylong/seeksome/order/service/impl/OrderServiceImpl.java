package com.zyzylong.seeksome.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.order.constant.OrderConstant;
import com.zyzylong.seeksome.order.controller.admin.request.AdminDefaultedOrderPageQueryRequest;
import com.zyzylong.seeksome.order.controller.admin.request.AdminOrderPageQueryRequest;
import com.zyzylong.seeksome.order.controller.admin.vo.AdminDefaultedOrderVO;
import com.zyzylong.seeksome.order.controller.admin.vo.AdminOrderVO;
import com.zyzylong.seeksome.order.dao.entity.AuctionOrderDO;
import com.zyzylong.seeksome.order.dao.mapper.AuctionOrderMapper;
import com.zyzylong.seeksome.order.enums.OrderStatusEnum;
import com.zyzylong.seeksome.order.exception.OrderException;
import com.zyzylong.seeksome.order.service.OrderService;
import com.zyzylong.seeksome.user.dao.entity.CreditLogDO;
import com.zyzylong.seeksome.user.dao.mapper.CreditLogMapper;
import com.zyzylong.seeksome.wallet.dao.entity.DepositLogDO;
import com.zyzylong.seeksome.wallet.dao.mapper.DepositLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现。
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final AuctionOrderMapper auctionOrderMapper;

    private final DepositLogMapper depositLogMapper;

    private final CreditLogMapper creditLogMapper;

    @Override
    public PageResult<AdminOrderVO> listOrders(AdminOrderPageQueryRequest requestParam) {
        LambdaQueryWrapper<AuctionOrderDO> queryWrapper = new LambdaQueryWrapper<>();
        String status = StrUtil.trimToNull(requestParam.getStatus());
        if (status != null) {
            boolean knownStatus = Arrays.stream(OrderStatusEnum.values())
                    .anyMatch(each -> each.getValue().equals(status));
            if (!knownStatus) {
                throw new OrderException(CommonErrorCodeEnum.PARAM_INVALID, "订单状态不合法");
            }
            queryWrapper.eq(AuctionOrderDO::getStatus, status);
        }
        queryWrapper.orderByDesc(AuctionOrderDO::getCreateTime).orderByDesc(AuctionOrderDO::getId);

        Page<AuctionOrderDO> page = auctionOrderMapper.selectPage(
                Page.of(requestParam.getPageNum(), requestParam.getPageSize()), queryWrapper);
        List<AdminOrderVO> list = page.getRecords().stream().map(this::toAdminOrderVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    @Override
    public PageResult<AdminDefaultedOrderVO> listDefaultedOrders(
            AdminDefaultedOrderPageQueryRequest requestParam) {
        LambdaQueryWrapper<AuctionOrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AuctionOrderDO::getStatus, OrderStatusEnum.DEFAULTED.getValue())
                .orderByDesc(AuctionOrderDO::getCreateTime)
                .orderByDesc(AuctionOrderDO::getId);

        Page<AuctionOrderDO> page = auctionOrderMapper.selectPage(
                Page.of(requestParam.getPageNum(), requestParam.getPageSize()), queryWrapper);
        List<AuctionOrderDO> orders = page.getRecords();
        Map<String, DepositLogDO> depositLogMap = listDepositDeducts(orders);
        Map<String, CreditLogDO> creditLogMap = listCreditLogs(orders);
        List<AdminDefaultedOrderVO> list = orders.stream()
                .map(each -> toAdminDefaultedOrderVO(each, depositLogMap, creditLogMap))
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    private Map<String, DepositLogDO> listDepositDeducts(List<AuctionOrderDO> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }
        List<Long> buyerIds = orders.stream().map(AuctionOrderDO::getBuyerId).distinct().toList();
        List<Long> auctionIds = orders.stream().map(AuctionOrderDO::getAuctionId).distinct().toList();
        List<DepositLogDO> depositLogs = depositLogMapper.selectList(new LambdaQueryWrapper<DepositLogDO>()
                .in(DepositLogDO::getUserId, buyerIds)
                .in(DepositLogDO::getAuctionId, auctionIds)
                .eq(DepositLogDO::getType, OrderConstant.DEPOSIT_TYPE_DEDUCT));
        Map<String, DepositLogDO> result = new HashMap<>();
        for (DepositLogDO each : depositLogs) {
            result.put(buildDepositKey(each.getUserId(), each.getAuctionId()), each);
        }
        return result;
    }

    private Map<String, CreditLogDO> listCreditLogs(List<AuctionOrderDO> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }
        List<Long> orderIds = orders.stream().map(AuctionOrderDO::getId).toList();
        List<CreditLogDO> creditLogs = creditLogMapper.selectList(new LambdaQueryWrapper<CreditLogDO>()
                .in(CreditLogDO::getRelatedId, orderIds)
                .eq(CreditLogDO::getRelatedType, OrderConstant.RELATED_TYPE_ORDER)
                .orderByDesc(CreditLogDO::getId));
        Map<String, CreditLogDO> result = new HashMap<>();
        for (CreditLogDO each : creditLogs) {
            result.putIfAbsent(buildCreditKey(each.getRelatedId(), each.getUserId()), each);
        }
        return result;
    }

    private String buildDepositKey(Long userId, Long auctionId) {
        return userId + ":" + auctionId;
    }

    private String buildCreditKey(Long orderId, Long userId) {
        return orderId + ":" + userId;
    }

    private AdminOrderVO toAdminOrderVO(AuctionOrderDO order) {
        return AdminOrderVO.builder()
                .orderId(order.getId())
                .auctionId(order.getAuctionId())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .finalPrice(order.getFinalPrice())
                .status(order.getStatus())
                .payDeadline(order.getPayDeadline())
                .paidAt(order.getPaidAt())
                .deliveredAt(order.getDeliveredAt())
                .completedAt(order.getCompletedAt())
                .createTime(order.getCreateTime())
                .build();
    }

    private AdminDefaultedOrderVO toAdminDefaultedOrderVO(AuctionOrderDO order,
                                                          Map<String, DepositLogDO> depositLogMap,
                                                          Map<String, CreditLogDO> creditLogMap) {
        DepositLogDO depositLog = depositLogMap.get(
                buildDepositKey(order.getBuyerId(), order.getAuctionId()));
        CreditLogDO creditLog = creditLogMap.get(buildCreditKey(order.getId(), order.getBuyerId()));
        return AdminDefaultedOrderVO.builder()
                .orderId(order.getId())
                .auctionId(order.getAuctionId())
                .buyerId(order.getBuyerId())
                .sellerId(order.getSellerId())
                .finalPrice(order.getFinalPrice())
                .payDeadline(order.getPayDeadline())
                .createTime(order.getCreateTime())
                .depositDeductAmount(depositLog == null ? null : depositLog.getAmount())
                .creditChange(creditLog == null ? null : creditLog.getChangeValue())
                .creditReason(creditLog == null ? null : creditLog.getReason())
                .penaltyTime(resolvePenaltyTime(creditLog, depositLog))
                .build();
    }

    private LocalDateTime resolvePenaltyTime(CreditLogDO creditLog, DepositLogDO depositLog) {
        if (creditLog != null) {
            return creditLog.getCreateTime();
        }
        return depositLog == null ? null : depositLog.getCreateTime();
    }
}
