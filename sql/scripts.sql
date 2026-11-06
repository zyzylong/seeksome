
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
