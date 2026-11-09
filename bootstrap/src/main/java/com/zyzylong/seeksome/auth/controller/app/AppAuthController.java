package com.zyzylong.seeksome.auth.controller.app;

import com.zyzylong.seeksome.auth.controller.app.request.AppLoginRequest;
import com.zyzylong.seeksome.auth.controller.app.request.AppRegisterRequest;
import com.zyzylong.seeksome.auth.controller.app.vo.AppLoginVO;
import com.zyzylong.seeksome.auth.service.AppAuthService;
import com.zyzylong.seeksome.framework.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端认证接口。
 */
@Tag(name = "用户端-用户认证", description = "用户注册、登录与登出")
@RestController
@RequestMapping("/app/auth")
@RequiredArgsConstructor
public class AppAuthController {

    private final AppAuthService appAuthService;

    @Operation(summary = "用户注册", description = "用户名全局唯一，密码以 BCrypt 哈希存储；注册后需另行登录")
    @PostMapping("/register")
    public Result<Long> saveUser(@Valid @RequestBody AppRegisterRequest requestParam) {
        return Result.success(appAuthService.saveUser(requestParam));
    }

    @Operation(summary = "用户登录", description = "校验用户名密码并签发用户会话 Token；冻结用户禁止登录")
    @PostMapping("/login")
    public Result<AppLoginVO> login(@Valid @RequestBody AppLoginRequest requestParam) {
        return Result.success(appAuthService.login(requestParam));
    }

    @Operation(summary = "用户登出", description = "使当前用户会话 Token 立即失效")
    @Parameter(name = "satoken", in = ParameterIn.HEADER, required = true,
            description = "用户会话 Token，取登录响应中的 data.token")
    @PostMapping("/logout")
    public Result<Void> logout() {
        appAuthService.logout();
        return Result.success(null);
    }
}
