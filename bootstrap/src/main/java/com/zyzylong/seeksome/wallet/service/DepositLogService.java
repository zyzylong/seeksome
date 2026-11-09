package com.zyzylong.seeksome.wallet.service;

import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.wallet.controller.admin.request.AdminDepositLogPageQueryRequest;
import com.zyzylong.seeksome.wallet.controller.admin.vo.AdminDepositLogVO;

/**
 * 保证金流水服务。
 */
public interface DepositLogService {

    PageResult<AdminDepositLogVO> listDepositLogs(AdminDepositLogPageQueryRequest requestParam);
}
