package com.zyzylong.seeksome.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 管理员登录响应。
 */
@Schema(description = "管理员登录响应")
@Data
@Builder
public class AdminLoginRespDTO {

    @Schema(description = "管理员会话 Token，同时写入响应头 admin-token",
            example = "3f2a6c1e-9b7d-4e2a-8c4f-1a2b3c4d5e6f")
    private String token;

    @Schema(description = "管理员 ID", example = "1")
    private Long adminId;

    @Schema(description = "管理员用户名", example = "admin")
    private String username;

    @Schema(description = "角色标识", example = "ADMIN")
    private String role;
}
