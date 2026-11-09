package com.zyzylong.seeksome.user.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信用分流水数据对象。
 */
@Data
@Builder
@TableName("credit_log")
public class CreditLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer changeValue;

    private String reason;

    private String relatedType;

    private Long relatedId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
