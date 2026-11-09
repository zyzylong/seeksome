package com.zyzylong.seeksome.order.controller.admin;

import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.framework.result.Result;
import com.zyzylong.seeksome.order.controller.admin.request.AdminDefaultedOrderPageQueryRequest;
import com.zyzylong.seeksome.order.controller.admin.request.AdminOrderPageQueryRequest;
import com.zyzylong.seeksome.order.controller.admin.vo.AdminDefaultedOrderVO;
import com.zyzylong.seeksome.order.controller.admin.vo.AdminOrderVO;
import com.zyzylong.seeksome.order.service.OrderService;
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
 * 管理端订单接口。
 */
@Tag(name = "管理端-订单管理", description = "订单列表与违约记录查询")
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "订单列表", description = "分页查询订单，支持按状态筛选；不传状态查全部")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @GetMapping
    public Result<PageResult<AdminOrderVO>> listOrders(@Valid AdminOrderPageQueryRequest requestParam) {
        return Result.success(orderService.listOrders(requestParam));
    }

    @Operation(summary = "违约记录", description = "分页查询违约订单，返回保证金扣罚与信用分扣分明细")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @GetMapping("/defaulted")
    public Result<PageResult<AdminDefaultedOrderVO>> listDefaultedOrders(
            @Valid AdminDefaultedOrderPageQueryRequest requestParam) {
        return Result.success(orderService.listDefaultedOrders(requestParam));
    }
}
