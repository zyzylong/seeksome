package com.zyzylong.seeksome.wallet.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zyzylong.seeksome.framework.error.CommonErrorCodeEnum;
import com.zyzylong.seeksome.framework.result.PageResult;
import com.zyzylong.seeksome.wallet.controller.admin.request.AdminDepositLogPageQueryRequest;
import com.zyzylong.seeksome.wallet.controller.admin.vo.AdminDepositLogVO;
import com.zyzylong.seeksome.wallet.dao.entity.DepositLogDO;
import com.zyzylong.seeksome.wallet.dao.mapper.DepositLogMapper;
import com.zyzylong.seeksome.wallet.enums.DepositLogTypeEnum;
import com.zyzylong.seeksome.wallet.exception.WalletException;
import com.zyzylong.seeksome.wallet.service.DepositLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 保证金流水服务实现。
 */
@Service
@RequiredArgsConstructor
public class DepositLogServiceImpl implements DepositLogService {

    private final DepositLogMapper depositLogMapper;

    @Override
    public PageResult<AdminDepositLogVO> listDepositLogs(AdminDepositLogPageQueryRequest requestParam) {
        LambdaQueryWrapper<DepositLogDO> queryWrapper = new LambdaQueryWrapper<>();
        String type = StrUtil.trimToNull(requestParam.getType());
        if (type != null) {
            boolean knownType = Arrays.stream(DepositLogTypeEnum.values())
                    .anyMatch(each -> each.getValue().equals(type));
            if (!knownType) {
                throw new WalletException(CommonErrorCodeEnum.PARAM_INVALID, "保证金流水类型不合法");
            }
            queryWrapper.eq(DepositLogDO::getType, type);
        }
        queryWrapper.orderByDesc(DepositLogDO::getCreateTime).orderByDesc(DepositLogDO::getId);

        Page<DepositLogDO> page = depositLogMapper.selectPage(
                Page.of(requestParam.getPageNum(), requestParam.getPageSize()), queryWrapper);
        List<AdminDepositLogVO> list = page.getRecords().stream().map(this::toAdminDepositLogVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    private AdminDepositLogVO toAdminDepositLogVO(DepositLogDO depositLog) {
        return AdminDepositLogVO.builder()
                .depositLogId(depositLog.getId())
                .userId(depositLog.getUserId())
                .auctionId(depositLog.getAuctionId())
                .amount(depositLog.getAmount())
                .type(depositLog.getType())
                .createTime(depositLog.getCreateTime())
                .build();
    }
}
