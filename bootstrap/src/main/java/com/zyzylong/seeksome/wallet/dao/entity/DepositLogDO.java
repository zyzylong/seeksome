package com.zyzylong.seeksome.wallet.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 保证金流水数据对象。
 */
@Data
@Builder
@TableName("deposit_log")
public class DepositLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long auctionId;

    private BigDecimal amount;

    private String type;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
