package com.zyzylong.seeksome.wallet.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包流水数据对象。
 */
@Data
@Builder
@TableName("wallet_log")
public class WalletLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal amount;

    private String type;

    private String relatedType;

    private Long relatedId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
