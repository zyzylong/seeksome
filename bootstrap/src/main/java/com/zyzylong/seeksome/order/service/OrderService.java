package com.zyzylong.seeksome.order.service;

import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.order.controller.admin.request.AdminDefaultedOrderPageQueryRequest;
import com.zyzylong.seeksome.order.controller.admin.request.AdminOrderPageQueryRequest;
import com.zyzylong.seeksome.order.controller.admin.vo.AdminDefaultedOrderVO;
import com.zyzylong.seeksome.order.controller.admin.vo.AdminOrderVO;

/**
 * 订单服务。
 */
public interface OrderService {

    PageResult<AdminOrderVO> listOrders(AdminOrderPageQueryRequest requestParam);

    PageResult<AdminDefaultedOrderVO> listDefaultedOrders(AdminDefaultedOrderPageQueryRequest requestParam);
}
