package com.zyzylong.seeksome.user.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户数据对象。
 */
@Data
@Builder
@TableName("user")
public class UserDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private Integer creditScore;

    private BigDecimal walletBalance;

    private BigDecimal frozenBalance;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
