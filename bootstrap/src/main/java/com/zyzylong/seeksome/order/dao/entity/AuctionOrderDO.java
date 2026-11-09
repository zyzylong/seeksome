package com.zyzylong.seeksome.order.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拍卖订单数据对象。
 */
@Data
@Builder
@TableName("auction_order")
public class AuctionOrderDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long auctionId;

    private Long buyerId;

    private Long sellerId;

    private BigDecimal finalPrice;

    private String status;

    private LocalDateTime payDeadline;

    private LocalDateTime paidAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime completedAt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
