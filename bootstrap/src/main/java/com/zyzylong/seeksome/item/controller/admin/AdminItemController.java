package com.zyzylong.seeksome.item.controller.admin;

import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.framework.result.Result;
import com.zyzylong.seeksome.item.controller.admin.request.AdminItemPageQueryRequest;
import com.zyzylong.seeksome.item.controller.admin.request.AdminItemRejectRequest;
import com.zyzylong.seeksome.item.controller.admin.vo.AdminItemVO;
import com.zyzylong.seeksome.item.service.AuctionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端拍品接口。
 */
@Tag(name = "管理端-拍品管理", description = "拍品审核与下架：待审核列表、审核通过、驳回、下架")
@RestController
@RequestMapping("/admin/items")
@RequiredArgsConstructor
public class AdminItemController {

    private final AuctionItemService auctionItemService;

    @Operation(summary = "拍品列表", description = "分页查询拍品，支持按状态、分类与卖家筛选；不传状态查全部")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @GetMapping
    public Result<PageResult<AdminItemVO>> listItems(@Valid AdminItemPageQueryRequest requestParam) {
        return Result.success(auctionItemService.listItems(requestParam));
    }

    @Operation(summary = "审核通过", description = "仅待审核拍品可操作；通过即开拍，开拍时间为当前时间，结束时间为开拍时间加拍卖时长")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @PostMapping("/{itemId}/approve")
    public Result<Void> approveItem(@PathVariable Long itemId) {
        auctionItemService.updateItemApproved(itemId);
        return Result.success(null);
    }

    @Operation(summary = "审核驳回", description = "仅待审核拍品可操作；驳回原因必填，卖家可查看")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @PostMapping("/{itemId}/reject")
    public Result<Void> rejectItem(@PathVariable Long itemId,
                                   @Valid @RequestBody AdminItemRejectRequest requestParam) {
        auctionItemService.updateItemRejected(itemId, requestParam);
        return Result.success(null);
    }

    @Operation(summary = "下架拍品", description = "违规拍品下架，仅待审核或在拍拍品可操作；下架后状态为 cancelled")
    @Parameter(name = "admin-token", in = ParameterIn.HEADER, required = true,
            description = "管理员会话 Token，取登录响应中的 data.token")
    @PostMapping("/{itemId}/cancel")
    public Result<Void> cancelItem(@PathVariable Long itemId) {
        auctionItemService.updateItemCancelled(itemId);
        return Result.success(null);
    }
}
