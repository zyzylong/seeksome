package com.zyzylong.seeksome.auth.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 用户登录响应。
 */
@Schema(description = "用户登录响应")
@Data
@Builder
public class AppLoginVO {

    @Schema(description = "用户会话 Token，同时写入响应头 satoken",
            example = "3f2a6c1e-9b7d-4e2a-8c4f-1a2b3c4d5e6f")
    private String token;

    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "xiaoming")
    private String username;

    @Schema(description = "昵称", example = "小明")
    private String nickname;
}
