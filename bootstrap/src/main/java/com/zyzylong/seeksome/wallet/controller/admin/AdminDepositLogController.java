package com.zyzylong.seeksome.wallet.controller.admin;

import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.framework.result.Result;
import com.zyzylong.seeksome.wallet.controller.admin.request.AdminDepositLogPageQueryRequest;
import com.zyzylong.seeksome.wallet.controller.admin.vo.AdminDepositLogVO;
import com.zyzylong.seeksome.wallet.service.DepositLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端保证金流水接口。
 */
@Tag(name = "管理端-保证金管理", description = "保证金扣罚日志查询")
@RestController
@RequestMapping("/admin/deposit-logs")
@RequiredArgsConstructor
public class AdminDepositLogController {

    private final DepositLogService depositLogService;

    @Operation(summary = "保证金扣罚日志", description = "分页查询保证金流水，支持按类型筛选；不传类型查全部")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @GetMapping
    public Result<PageResult<AdminDepositLogVO>> listDepositLogs(
            @Valid AdminDepositLogPageQueryRequest requestParam) {
        return Result.success(depositLogService.listDepositLogs(requestParam));
    }
}
