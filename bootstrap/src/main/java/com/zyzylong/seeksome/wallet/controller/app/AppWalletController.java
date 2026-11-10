package com.zyzylong.seeksome.wallet.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.framework.result.Result;
import com.zyzylong.seeksome.user.service.UserService;
import com.zyzylong.seeksome.wallet.controller.app.request.AppWalletLogPageQueryRequest;
import com.zyzylong.seeksome.wallet.controller.app.request.AppWalletRechargeRequest;
import com.zyzylong.seeksome.wallet.controller.app.vo.AppWalletLogVO;
import com.zyzylong.seeksome.wallet.controller.app.vo.AppWalletVO;
import com.zyzylong.seeksome.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端钱包接口。
 */
@Tag(name = "用户端-钱包", description = "钱包余额与冻结金额、模拟充值、钱包流水")
@RestController
@RequestMapping("/app/wallets")
@RequiredArgsConstructor
public class AppWalletController {

    private final WalletService walletService;

    private final UserService userService;

    @Operation(summary = "钱包余额", description = "返回当前登录用户的可用余额与冻结金额")
    @Parameter(name = "satoken", in = ParameterIn.HEADER, required = true,
            description = "用户会话 Token，取登录响应中的 data.token")
    @GetMapping
    public Result<AppWalletVO> getWallet() {
        return Result.success(walletService.getWallet(getAccessibleUserId()));
    }

    @Operation(summary = "模拟充值", description = "按金额充值到账并记一条 recharge 流水，返回最新余额；单笔 0.01 ~ 10000.00 元")
    @Parameter(name = "satoken", in = ParameterIn.HEADER, required = true,
            description = "用户会话 Token，取登录响应中的 data.token")
    @PostMapping("/recharge")
    public Result<AppWalletVO> saveRecharge(@Valid @RequestBody AppWalletRechargeRequest requestParam) {
        return Result.success(walletService.saveRecharge(getAccessibleUserId(), requestParam));
    }

    @Operation(summary = "钱包流水", description = "分页查询当前用户的钱包流水，按时间倒序")
    @Parameter(name = "satoken", in = ParameterIn.HEADER, required = true,
            description = "用户会话 Token，取登录响应中的 data.token")
    @GetMapping("/logs")
    public Result<PageResult<AppWalletLogVO>> listWalletLogs(
            @Valid AppWalletLogPageQueryRequest requestParam) {
        return Result.success(walletService.listWalletLogs(getAccessibleUserId(), requestParam));
    }

    private Long getAccessibleUserId() {
        StpUtil.checkLogin();
        Long userId = StpUtil.getLoginIdAsLong();
        userService.checkUserAccessible(userId);
        return userId;
    }
}
