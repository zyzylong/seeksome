package com.zyzylong.seeksome.wallet.service;

import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.wallet.controller.app.request.AppWalletLogPageQueryRequest;
import com.zyzylong.seeksome.wallet.controller.app.request.AppWalletRechargeRequest;
import com.zyzylong.seeksome.wallet.controller.app.vo.AppWalletLogVO;
import com.zyzylong.seeksome.wallet.controller.app.vo.AppWalletVO;

/**
 * 钱包服务。
 */
public interface WalletService {

    AppWalletVO getWallet(Long userId);

    AppWalletVO saveRecharge(Long userId, AppWalletRechargeRequest requestParam);

    PageResult<AppWalletLogVO> listWalletLogs(Long userId, AppWalletLogPageQueryRequest requestParam);
}
