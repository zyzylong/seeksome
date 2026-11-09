package com.zyzylong.seeksome.item.service;

import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.item.controller.admin.request.AdminItemPageQueryRequest;
import com.zyzylong.seeksome.item.controller.admin.request.AdminItemRejectRequest;
import com.zyzylong.seeksome.item.controller.admin.vo.AdminItemVO;

/**
 * 拍品管理服务。
 */
public interface AuctionItemService {

    PageResult<AdminItemVO> listItems(AdminItemPageQueryRequest requestParam);

    void updateItemApproved(Long itemId);

    void updateItemRejected(Long itemId, AdminItemRejectRequest requestParam);

    void updateItemCancelled(Long itemId);
}
