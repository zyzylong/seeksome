package com.zyzylong.seeksome.item.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拍品数据对象。
 */
@Data
@Builder
@TableName("auction_item")
public class AuctionItemDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;

    private String title;

    private String description;

    private Long categoryId;

    private String conditionLevel;

    private String images;

    private BigDecimal startPrice;

    private BigDecimal minIncrement;

    private Integer durationHours;

    private BigDecimal currentPrice;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private String rejectReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
