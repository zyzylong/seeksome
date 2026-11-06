package com.zyzylong.seeksome.admin.service;

import com.zyzylong.seeksome.admin.domain.dto.AdminInfoRespDTO;
import com.zyzylong.seeksome.admin.domain.dto.AdminLoginReqDTO;
import com.zyzylong.seeksome.admin.domain.dto.AdminLoginRespDTO;

/**
 * 管理员认证服务。
 */
public interface AdminAuthService {

    AdminLoginRespDTO login(AdminLoginReqDTO requestParam, String clientIp, String userAgent);

    AdminInfoRespDTO getCurrentAdminInfo(Long adminId);

    void logout(Long adminId, String clientIp, String userAgent);
}
