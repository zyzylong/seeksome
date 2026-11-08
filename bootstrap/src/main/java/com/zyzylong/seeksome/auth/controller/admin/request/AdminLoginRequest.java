package com.zyzylong.seeksome.auth.controller.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * 管理员登录请求。
 */
@Schema(description = "管理员登录请求")
@Data
@Builder
public class AdminLoginRequest {

    @Schema(description = "管理员用户名，首尾空白会被去除", example = "admin")
    @NotBlank
    @Size(max = 64)
    private String username;

    @Schema(description = "登录密码，明文传输须使用 HTTPS", example = "Passw0rd!")
    @NotBlank
    @Size(max = 100)
    private String password;
}
