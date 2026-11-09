-- 演示种子数据脚本，仅用于本地与演示环境
-- 使用前请先执行 sql/scripts.sql 完成建库建表
USE `seeksome`;

-- 初始化管理员账号，密码 123456，可重复执行
INSERT INTO `admin_user` (`username`, `password`, `role`, `is_enabled`)
VALUES ('admin', '$2a$10$LdADrYshKCRRlU0It6rfneKprcPog.A.plegY/xQt/Ma8FBSNi5w2', 'ADMIN', 1) AS new
ON DUPLICATE KEY UPDATE `password` = new.`password`,
                        `role` = new.`role`,
                        `is_enabled` = new.`is_enabled`;

-- 拍品分类种子数据，可重复执行
INSERT INTO `category` (`code`, `name`, `sort`, `is_enabled`)
VALUES ('digital', '数码', 1, 1),
       ('fashion', '穿搭', 2, 1),
       ('home', '家居', 3, 1),
       ('media', '书影音', 4, 1),
       ('collection', '收藏', 5, 1) AS new
ON DUPLICATE KEY UPDATE `name` = new.`name`,
                        `sort` = new.`sort`,
                        `is_enabled` = new.`is_enabled`;

-- 种子用户，统一密码 123456，可重复执行
-- 冻结金额暂为 0，待拍品与保证金种子数据落地后按 deposit_log 设置
INSERT INTO `user` (`username`, `password`, `nickname`, `avatar`, `credit_score`, `wallet_balance`,
                    `frozen_balance`, `status`)
VALUES ('user_zhangsan', '$2a$10$leN2Xd8jn1gamueSA3ZVm.QGRYMxx3LexEYMw8el4ChN/BQBtueg6', '张三', NULL, 100, 500.00, 0.00, 'normal'),
       ('user_lisi', '$2a$10$leN2Xd8jn1gamueSA3ZVm.QGRYMxx3LexEYMw8el4ChN/BQBtueg6', '李四', NULL, 100, 1200.50, 0.00, 'normal'),
       ('user_wangwu', '$2a$10$leN2Xd8jn1gamueSA3ZVm.QGRYMxx3LexEYMw8el4ChN/BQBtueg6', '王五', NULL, 80, 80.00, 0.00, 'normal'),
       ('user_zhaoliu', '$2a$10$leN2Xd8jn1gamueSA3ZVm.QGRYMxx3LexEYMw8el4ChN/BQBtueg6', '赵六', NULL, 60, 0.00, 0.00, 'frozen'),
       ('user_sunqi', '$2a$10$leN2Xd8jn1gamueSA3ZVm.QGRYMxx3LexEYMw8el4ChN/BQBtueg6', '孙七', NULL, 100, 3000.00, 0.00, 'normal') AS new
ON DUPLICATE KEY UPDATE `nickname` = new.`nickname`,
                        `avatar` = new.`avatar`,
                        `credit_score` = new.`credit_score`,
                        `wallet_balance` = new.`wallet_balance`,
                        `frozen_balance` = new.`frozen_balance`,
                        `status` = new.`status`;
