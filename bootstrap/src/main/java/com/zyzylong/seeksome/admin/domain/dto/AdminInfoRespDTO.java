package com.zyzylong.seeksome.admin.domain.dto;

import lombok.Data;

/**
 * 当前登录管理员信息。
 */
@Data
public class AdminInfoRespDTO {

    private Long adminId;

    private String username;

    private String role;
}
