package com.zyzylong.seeksome.admin.domain.dto;

import lombok.Data;

/**
 * 管理员登录响应。
 */
@Data
public class AdminLoginRespDTO {

    private String token;

    private Long adminId;

    private String username;

    private String role;
}
