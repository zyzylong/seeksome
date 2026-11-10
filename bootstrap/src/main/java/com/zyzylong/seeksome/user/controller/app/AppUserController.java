package com.zyzylong.seeksome.user.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.framework.result.Result;
import com.zyzylong.seeksome.user.controller.app.request.AppCreditLogPageQueryRequest;
import com.zyzylong.seeksome.user.controller.app.request.AppUserProfileUpdateRequest;
import com.zyzylong.seeksome.user.controller.app.vo.AppCreditLogVO;
import com.zyzylong.seeksome.user.controller.app.vo.AppUserProfileVO;
import com.zyzylong.seeksome.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端个人中心接口。
 */
@Tag(name = "用户端-个人中心", description = "个人资料查看与编辑、信用分与违约记录")
@RestController
@RequestMapping("/app/users")
@RequiredArgsConstructor
public class AppUserController {

    private final UserService userService;

    @Operation(summary = "查看个人资料", description = "返回当前登录用户的昵称、头像与信用分")
    @Parameter(name = "satoken", in = ParameterIn.HEADER, required = true,
            description = "用户会话 Token，取登录响应中的 data.token")
    @GetMapping("/profile")
    public Result<AppUserProfileVO> getUserProfile() {
        return Result.success(userService.getUserProfile(getAccessibleUserId()));
    }

    @Operation(summary = "编辑个人资料", description = "更新昵称与头像；头像不传保持原值，传空字符串清空头像")
    @Parameter(name = "satoken", in = ParameterIn.HEADER, required = true,
            description = "用户会话 Token，取登录响应中的 data.token")
    @PutMapping("/profile")
    public Result<AppUserProfileVO> updateUserProfile(
            @Valid @RequestBody AppUserProfileUpdateRequest requestParam) {
        return Result.success(userService.updateUserProfile(getAccessibleUserId(), requestParam));
    }

    @Operation(summary = "信用分与违约记录", description = "分页查询当前用户的信用分变动记录，即违约扣罚明细")
    @Parameter(name = "satoken", in = ParameterIn.HEADER, required = true,
            description = "用户会话 Token，取登录响应中的 data.token")
    @GetMapping("/credit-logs")
    public Result<PageResult<AppCreditLogVO>> listCreditLogs(
            @Valid AppCreditLogPageQueryRequest requestParam) {
        return Result.success(userService.listCreditLogs(getAccessibleUserId(), requestParam));
    }

    private Long getAccessibleUserId() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        userService.checkUserAccessible(userId);
        return userId;
    }
}
