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

-- 种子拍品，固定主键保证可重复执行；卖家与分类通过用户名/编码关联
-- 在拍拍品的开拍与结束时间按当前时间滚动计算，便于演示
INSERT INTO `auction_item` (`id`, `seller_id`, `title`, `description`, `category_id`, `condition_level`,
                            `images`, `start_price`, `min_increment`, `duration_hours`, `current_price`,
                            `start_time`, `end_time`, `status`, `reject_reason`)
VALUES (1, (SELECT `id` FROM `user` WHERE `username` = 'user_zhangsan'),
        '九成新 iPhone 15 Pro 256G 原色钛金属',
        '自用一年，无磕碰，电池健康 92%，原装充电线与包装盒齐全。',
        (SELECT `id` FROM `category` WHERE `code` = 'digital'),
        '90_new', NULL, 4500.00, 100.00, 48, NULL, NULL, NULL, 'pending_review', NULL),
       (2, (SELECT `id` FROM `user` WHERE `username` = 'user_lisi'),
        '复古牛皮单肩包 日常通勤',
        '植鞣牛皮，五金无掉色，内衬干净，附防尘袋。',
        (SELECT `id` FROM `category` WHERE `code` = 'fashion'),
        '80_new', NULL, 180.00, 20.00, 24, NULL, NULL, NULL, 'pending_review', NULL),
       (3, (SELECT `id` FROM `user` WHERE `username` = 'user_sunqi'),
        '2019 年纪念邮票整版',
        '整版未折，保存于干燥箱，适合收藏入门。',
        (SELECT `id` FROM `category` WHERE `code` = 'collection'),
        '95_new', NULL, 260.00, 30.00, 72, NULL, NULL, NULL, 'pending_review', NULL),
       (4, (SELECT `id` FROM `user` WHERE `username` = 'user_lisi'),
        '北欧原木边几 九成新',
        '白蜡木实木，无划痕，同城可自提。',
        (SELECT `id` FROM `category` WHERE `code` = 'home'),
        '90_new', NULL, 120.00, 10.00, 48, NULL,
        DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 46 HOUR), 'active', NULL),
       (5, (SELECT `id` FROM `user` WHERE `username` = 'user_zhangsan'),
        '村上春树作品集 6 册',
        '正版旧书，无笔记无划线，书脊完好。',
        (SELECT `id` FROM `category` WHERE `code` = 'media'),
        '80_new', NULL, 60.00, 5.00, 24, NULL,
        DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 23 HOUR), 'active', NULL),
       (6, (SELECT `id` FROM `user` WHERE `username` = 'user_wangwu'),
        '旧款蓝牙耳机 有磨损',
        '功能正常，充电仓有明显使用痕迹。',
        (SELECT `id` FROM `category` WHERE `code` = 'digital'),
        'flawed', NULL, 40.00, 5.00, 24, NULL, NULL, NULL, 'rejected',
        '图片不清晰，请补充完整实物图后重新发布') AS new
ON DUPLICATE KEY UPDATE `seller_id` = new.`seller_id`,
                        `title` = new.`title`,
                        `description` = new.`description`,
                        `category_id` = new.`category_id`,
                        `condition_level` = new.`condition_level`,
                        `images` = new.`images`,
                        `start_price` = new.`start_price`,
                        `min_increment` = new.`min_increment`,
                        `duration_hours` = new.`duration_hours`,
                        `current_price` = new.`current_price`,
                        `start_time` = new.`start_time`,
                        `end_time` = new.`end_time`,
                        `status` = new.`status`,
                        `reject_reason` = new.`reject_reason`;
