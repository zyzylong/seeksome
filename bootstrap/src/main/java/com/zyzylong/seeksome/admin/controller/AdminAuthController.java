package com.zyzylong.seeksome.admin.controller;

import com.zyzylong.seeksome.admin.auth.StpAdminUtil;
import com.zyzylong.seeksome.admin.domain.dto.AdminInfoRespDTO;
import com.zyzylong.seeksome.admin.domain.dto.AdminLoginReqDTO;
import com.zyzylong.seeksome.admin.domain.dto.AdminLoginRespDTO;
import com.zyzylong.seeksome.admin.service.AdminAuthService;
import com.zyzylong.seeksome.framework.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "管理端-管理员认证", description = "管理员登录、登出与当前登录信息查询")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Operation(summary = "管理员登录", description = "校验用户名密码并签发管理员会话 Token；连续失败会锁定账号，单 IP 失败过多会触发限流")
    @PostMapping("/login")
    public Result<AdminLoginRespDTO> login(@Valid @RequestBody AdminLoginReqDTO requestParam,
                                           HttpServletRequest httpRequest) {
        AdminLoginRespDTO result = adminAuthService.login(requestParam,
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return Result.success(result);
    }

    @Operation(summary = "管理员登出", description = "使当前管理员会话 Token 立即失效")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest httpRequest) {
        adminAuthService.logout(StpAdminUtil.getLoginIdAsLong(),
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return Result.success(null);
    }

    @Operation(summary = "获取当前管理员信息", description = "重新校验账号状态，账号被删除或禁用时强制注销并返回 401")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @GetMapping("/me")
    public Result<AdminInfoRespDTO> getCurrentAdminInfo() {
        return Result.success(adminAuthService.getCurrentAdminInfo(StpAdminUtil.getLoginIdAsLong()));
    }
}
