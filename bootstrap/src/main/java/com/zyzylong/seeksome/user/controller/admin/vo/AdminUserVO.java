package com.zyzylong.seeksome.user.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端用户列表项。
 */
@Schema(description = "管理端用户列表项")
@Data
@Builder
public class AdminUserVO {

    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "user_zhangsan")
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "信用分", example = "100")
    private Integer creditScore;

    @Schema(description = "钱包余额", example = "500.00")
    private BigDecimal walletBalance;

    @Schema(description = "冻结金额", example = "10.00")
    private BigDecimal frozenBalance;

    @Schema(description = "用户状态：normal-正常，frozen-冻结", example = "normal")
    private String status;

    @Schema(description = "注册时间", example = "2026-11-08T10:00:00")
    private LocalDateTime createdAt;
}
