package com.zyzylong.seeksome.user.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户个人资料。
 */
@Schema(description = "用户个人资料")
@Data
@Builder
public class AppUserProfileVO {

    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "xiaoming")
    private String username;

    @Schema(description = "昵称", example = "小明")
    private String nickname;

    @Schema(description = "头像地址", example = "/files/avatar/xiaoming.png")
    private String avatar;

    @Schema(description = "信用分", example = "100")
    private Integer creditScore;

    @Schema(description = "注册时间", example = "2026-11-08T10:00:00")
    private LocalDateTime createTime;
}
