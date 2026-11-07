package com.zyzylong.seeksome.admin.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 当前登录管理员信息。
 */
@Schema(description = "当前登录管理员信息")
@Data
@Builder
public class AdminInfoRespDTO {

    @Schema(description = "管理员 ID", example = "1")
    private Long adminId;

    @Schema(description = "管理员用户名", example = "admin")
    private String username;

    @Schema(description = "角色标识", example = "ADMIN")
    private String role;
}
