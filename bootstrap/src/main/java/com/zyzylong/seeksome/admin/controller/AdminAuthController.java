package com.zyzylong.seeksome.admin.controller;

import com.zyzylong.seeksome.admin.auth.StpAdminUtil;
import com.zyzylong.seeksome.admin.domain.dto.AdminInfoRespDTO;
import com.zyzylong.seeksome.admin.domain.dto.AdminLoginReqDTO;
import com.zyzylong.seeksome.admin.domain.dto.AdminLoginRespDTO;
import com.zyzylong.seeksome.admin.service.AdminAuthService;
import com.zyzylong.seeksome.framework.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员认证接口。
 */
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<AdminLoginRespDTO> login(@Valid @RequestBody AdminLoginReqDTO requestParam,
                                           HttpServletRequest httpRequest) {
        AdminLoginRespDTO result = adminAuthService.login(requestParam,
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return Result.success(result);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest httpRequest) {
        adminAuthService.logout(StpAdminUtil.getLoginIdAsLong(),
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return Result.success(null);
    }

    @GetMapping("/me")
    public Result<AdminInfoRespDTO> getCurrentAdminInfo() {
        return Result.success(adminAuthService.getCurrentAdminInfo(StpAdminUtil.getLoginIdAsLong()));
    }
}
