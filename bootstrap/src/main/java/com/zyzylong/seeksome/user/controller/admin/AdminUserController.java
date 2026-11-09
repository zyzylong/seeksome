package com.zyzylong.seeksome.user.controller.admin;

import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.framework.result.Result;
import com.zyzylong.seeksome.user.controller.admin.request.AdminUserPageQueryRequest;
import com.zyzylong.seeksome.user.controller.admin.vo.AdminUserVO;
import com.zyzylong.seeksome.user.enums.UserStatusEnum;
import com.zyzylong.seeksome.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端用户接口。
 */
@Tag(name = "管理端-用户管理", description = "管理端用户查询与冻结/解冻")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "用户列表", description = "分页查询用户，支持按用户名或昵称模糊搜索，返回信用分与钱包概览")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @GetMapping
    public Result<PageResult<AdminUserVO>> listUsers(@Valid AdminUserPageQueryRequest requestParam) {
        return Result.success(userService.listUsers(requestParam));
    }

    @Operation(summary = "冻结用户", description = "冻结后该用户禁止登录与交易；重复冻结幂等")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @PostMapping("/{userId}/freeze")
    public Result<Void> freezeUser(@PathVariable Long userId) {
        userService.updateUserStatus(userId, UserStatusEnum.FROZEN);
        return Result.success(null);
    }

    @Operation(summary = "解冻用户", description = "解除用户冻结状态，恢复登录与交易；重复解冻幂等")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @PostMapping("/{userId}/unfreeze")
    public Result<Void> unfreezeUser(@PathVariable Long userId) {
        userService.updateUserStatus(userId, UserStatusEnum.NORMAL);
        return Result.success(null);
    }
}
