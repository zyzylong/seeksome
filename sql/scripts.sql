
CREATE DATABASE IF NOT EXISTS `seeksome`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `seeksome`;

-- 管理员账户表，对应管理员登录功能
-- 密码仅存 BCrypt 哈希，禁止明文；出于安全要求不内置默认账号
CREATE TABLE IF NOT EXISTS `admin_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(64) NOT NULL COMMENT '管理员用户名',
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    `role` VARCHAR(32) NOT NULL DEFAULT 'ADMIN' COMMENT '角色标识',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `failed_count` INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    `locked_until` DATETIME NULL DEFAULT NULL COMMENT '锁定截止时间，NULL 表示未锁定',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_user_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '管理员账户表';

-- 初始化管理员账号（按需手工执行）：
-- 1. 使用 BCrypt 生成密码哈希，例如 Hutool：BCrypt.hashpw("明文密码", BCrypt.gensalt())
-- 2. 执行以下语句（替换占位符）：
-- INSERT INTO `admin_user` (`username`, `password`, `role`, `status`)
-- VALUES ('<管理员用户名>', '<BCrypt 哈希>', 'ADMIN', 1);

-- 用户表，对应管理端用户列表与用户端资料、信用分、钱包概览
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    `nickname` VARCHAR(64) NOT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) NULL DEFAULT NULL COMMENT '头像地址',
    `credit_score` INT NOT NULL DEFAULT 100 COMMENT '信用分，初始 100，违约 -20',
    `wallet_balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '钱包余额',
    `frozen_balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
    `status` VARCHAR(16) NOT NULL DEFAULT 'normal' COMMENT '状态：normal-正常，frozen-冻结',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户表';

-- 种子用户，统一密码 123456，可重复执行
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `credit_score`, `wallet_balance`,
                    `frozen_balance`, `status`)
VALUES ('user_zhangsan', '$2a$10$leN2Xd8jn1gamueSA3ZVm.QGRYMxx3LexEYMw8el4ChN/BQBtueg6', '张三', NULL, 100, 500.00, 0.00, 'normal'),
       ('user_lisi', '$2a$10$leN2Xd8jn1gamueSA3ZVm.QGRYMxx3LexEYMw8el4ChN/BQBtueg6', '李四', NULL, 100, 1200.50, 10.00, 'normal'),
       ('user_wangwu', '$2a$10$leN2Xd8jn1gamueSA3ZVm.QGRYMxx3LexEYMw8el4ChN/BQBtueg6', '王五', NULL, 80, 80.00, 0.00, 'normal'),
       ('user_zhaoliu', '$2a$10$leN2Xd8jn1gamueSA3ZVm.QGRYMxx3LexEYMw8el4ChN/BQBtueg6', '赵六', NULL, 60, 0.00, 0.00, 'frozen'),
       ('user_sunqi', '$2a$10$leN2Xd8jn1gamueSA3ZVm.QGRYMxx3LexEYMw8el4ChN/BQBtueg6', '孙七', NULL, 100, 3000.00, 10.00, 'normal') AS new
ON DUPLICATE KEY UPDATE `nickname` = new.`nickname`,
                        `avatar` = new.`avatar`,
                        `credit_score` = new.`credit_score`,
                        `wallet_balance` = new.`wallet_balance`,
                        `frozen_balance` = new.`frozen_balance`,
                        `status` = new.`status`;
