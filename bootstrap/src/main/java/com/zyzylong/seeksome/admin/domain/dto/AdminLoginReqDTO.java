package com.zyzylong.seeksome.admin.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 管理员登录请求。
 */
@Data
public class AdminLoginReqDTO {

    @NotBlank
    @Size(max = 64)
    private String username;

    @NotBlank
    @Size(max = 100)
    private String password;
}
