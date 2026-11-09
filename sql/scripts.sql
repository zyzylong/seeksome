
CREATE DATABASE IF NOT EXISTS `seeksome`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `seeksome`;

-- 管理员账户表，对应管理员登录功能
-- 密码仅存 BCrypt 哈希，禁止明文；演示账号由 seed.sql 提供，仅限本地演示环境
CREATE TABLE IF NOT EXISTS `admin_user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(64) NOT NULL COMMENT '管理员用户名',
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    `role` VARCHAR(32) NOT NULL DEFAULT 'ADMIN' COMMENT '角色标识',
    `is_enabled` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
    `failed_count` INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    `locked_until` DATETIME NULL DEFAULT NULL COMMENT '锁定截止时间，NULL 表示未锁定',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '管理员账户表';

-- 用户表，对应管理端用户列表与用户端资料、信用分、钱包概览
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) NULL DEFAULT NULL COMMENT '头像地址',
    `credit_score` INT NOT NULL DEFAULT 100 COMMENT '信用分，初始 100，违约 -20',
    `wallet_balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '钱包余额',
    `frozen_balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
    `status` VARCHAR(16) NOT NULL DEFAULT 'normal' COMMENT '状态：normal-正常，frozen-冻结',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户表';

-- 拍品分类表，管理端维护的数据字典，禁用代替删除
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code` VARCHAR(32) NOT NULL COMMENT '分类编码，如 digital',
    `name` VARCHAR(32) NOT NULL COMMENT '分类名称',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重，越小越靠前',
    `is_enabled` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '拍品分类表';

-- 拍品表，对应拍品发布、审核、拍卖大厅与拍品详情
CREATE TABLE IF NOT EXISTS `auction_item` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `seller_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家用户 ID',
    `title` VARCHAR(128) NOT NULL COMMENT '标题',
    `description` TEXT NULL DEFAULT NULL COMMENT '描述',
    `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类 ID，关联 category 表',
    `condition_level` VARCHAR(32) NOT NULL COMMENT '成色：95_new-95新，90_new-9成新，80_new-8成新，flawed-微瑕',
    `images` VARCHAR(255) NULL DEFAULT NULL COMMENT '图片地址，MVP 单图',
    `start_price` DECIMAL(10, 2) NOT NULL COMMENT '起拍价',
    `min_increment` DECIMAL(10, 2) NOT NULL COMMENT '最小加价幅度',
    `duration_hours` TINYINT UNSIGNED NOT NULL DEFAULT 48 COMMENT '拍卖时长（小时）：24/48/72，默认 48',
    `current_price` DECIMAL(10, 2) NULL DEFAULT NULL COMMENT '当前价，无出价时为 NULL',
    `start_time` DATETIME NULL DEFAULT NULL COMMENT '开拍时间，审核通过时置为当前时间',
    `end_time` DATETIME NULL DEFAULT NULL COMMENT '结束时间，开拍时间加拍卖时长',
    `status` VARCHAR(16) NOT NULL DEFAULT 'pending_review' COMMENT '状态：pending_review-待审核，active-在拍，ended-已结束，rejected-已驳回，cancelled-已下架',
    `reject_reason` VARCHAR(255) NULL DEFAULT NULL COMMENT '驳回原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status_end_time` (`status`, `end_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '拍品表';

-- 出价记录表，只插入不修改，完整留存竞价过程
CREATE TABLE IF NOT EXISTS `bid` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `auction_id` BIGINT UNSIGNED NOT NULL COMMENT '拍品 ID',
    `bidder_id` BIGINT UNSIGNED NOT NULL COMMENT '出价人用户 ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '出价金额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auction_id_amount` (`auction_id`, `amount`),
    KEY `idx_bidder_id` (`bidder_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '出价记录表';

-- 保证金流水表，对应保证金冻结、解冻、扣除记录
CREATE TABLE IF NOT EXISTS `deposit_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
    `auction_id` BIGINT UNSIGNED NOT NULL COMMENT '拍品 ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '金额',
    `type` VARCHAR(16) NOT NULL COMMENT '类型：freeze-冻结，unfreeze-解冻，deduct-扣除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id_auction_id_type` (`user_id`, `auction_id`, `type`),
    KEY `idx_auction_id` (`auction_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '保证金流水表';

-- 钱包流水表，对应充值、冻结、解冻、扣除、支付、入账记录
CREATE TABLE IF NOT EXISTS `wallet_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '金额',
    `type` VARCHAR(16) NOT NULL COMMENT '类型：recharge-充值，freeze-冻结，unfreeze-解冻，deduct-扣除，pay-支付，income-入账',
    `related_type` VARCHAR(32) NULL DEFAULT NULL COMMENT '关联业务类型：auction-拍品，order-订单',
    `related_id` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT '关联业务 ID，配合 related_type 使用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '钱包流水表';

-- 订单表，独立状态机承载成交与履约环节
CREATE TABLE IF NOT EXISTS `auction_order` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `auction_id` BIGINT UNSIGNED NOT NULL COMMENT '拍品 ID',
    `buyer_id` BIGINT UNSIGNED NOT NULL COMMENT '买家用户 ID',
    `seller_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家用户 ID',
    `final_price` DECIMAL(10, 2) NOT NULL COMMENT '成交价',
    `status` VARCHAR(16) NOT NULL DEFAULT 'pending_payment' COMMENT '状态：pending_payment-待支付，paid-已支付，delivered-已交付，completed-已完成，defaulted-已违约',
    `pay_deadline` DATETIME NULL DEFAULT NULL COMMENT '支付截止时间，生成后 30 分钟内',
    `paid_at` DATETIME NULL DEFAULT NULL COMMENT '支付时间',
    `delivered_at` DATETIME NULL DEFAULT NULL COMMENT '交付时间',
    `completed_at` DATETIME NULL DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auction_id` (`auction_id`),
    KEY `idx_buyer_id` (`buyer_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_status_pay_deadline` (`status`, `pay_deadline`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '订单表';

-- 信用分流水表，对应信用分变动记录
CREATE TABLE IF NOT EXISTS `credit_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户 ID',
    `change_value` INT NOT NULL COMMENT '信用分变动值，违约 -20',
    `reason` VARCHAR(255) NOT NULL COMMENT '变动原因',
    `related_type` VARCHAR(32) NULL DEFAULT NULL COMMENT '关联业务类型：order-订单',
    `related_id` BIGINT UNSIGNED NULL DEFAULT NULL COMMENT '关联业务 ID，配合 related_type 使用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '信用分流水表';
