package com.zyzylong.seeksome.auth.service;

import com.zyzylong.seeksome.auth.controller.admin.request.AdminLoginRequest;
import com.zyzylong.seeksome.auth.controller.admin.vo.AdminInfoVO;
import com.zyzylong.seeksome.auth.controller.admin.vo.AdminLoginVO;

/**
 * 管理员认证服务。
 */
public interface AdminAuthService {

    AdminLoginVO login(AdminLoginRequest requestParam, String clientIp, String userAgent);

    AdminInfoVO getCurrentAdminInfo(Long adminId);

    void logout(Long adminId, String clientIp, String userAgent);
}
