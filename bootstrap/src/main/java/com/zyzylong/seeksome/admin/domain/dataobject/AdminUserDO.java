package com.zyzylong.seeksome.admin.domain.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员账户数据对象。
 */
@Data
@Builder
@TableName("admin_user")
public class AdminUserDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String role;

    private Integer status;

    private Integer failedCount;

    private LocalDateTime lockedUntil;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
