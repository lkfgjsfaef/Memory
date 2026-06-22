-- ============================================
-- Memory 情侣回忆共享应用 - 数据库结构
-- Database: memory_db
-- Charset: utf8mb4
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table: users (用户表)
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录用户名',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BCrypt加密密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- Table: couple (情侣信息表)
-- ----------------------------
DROP TABLE IF EXISTS `couple`;
CREATE TABLE `couple` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `his_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '他的名字',
  `her_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '她的名字',
  `love_start_date` date NOT NULL COMMENT '恋爱开始日期',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情侣信息表';

-- ----------------------------
-- Table: important_date (重要日子表)
-- ----------------------------
DROP TABLE IF EXISTS `important_date`;
CREATE TABLE `important_date` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '📅' COMMENT '图标emoji',
  `event_date` date DEFAULT NULL COMMENT '具体日期（公历）',
  `lunar_date` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '农历日期描述',
  `note` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `recurring` tinyint(1) DEFAULT 0 COMMENT '是否每年重复 0-否 1-是',
  `recurring_month` int DEFAULT NULL COMMENT '重复月份（1-12）',
  `recurring_day` int DEFAULT NULL COMMENT '重复日期（1-31）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='重要日子表';

-- ----------------------------
-- Table: daily_record (日常记录表)
-- ----------------------------
DROP TABLE IF EXISTS `daily_record`;
CREATE TABLE `daily_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '正文内容',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作者',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地点',
  `mood` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '心情',
  `mood_icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '心情图标',
  `record_date` date NOT NULL COMMENT '记录日期',
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '图片URL列表，逗号分隔',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日常记录表';

-- ----------------------------
-- Table: wish (心愿清单表)
-- ----------------------------
DROP TABLE IF EXISTS `wish`;
CREATE TABLE `wish` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '心愿标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类（未来规划/旅行计划/生活目标）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT '状态 pending/completed',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发起人',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户',
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '心愿配图列表，逗号分隔',
  `start_date` date DEFAULT NULL COMMENT '发起日期',
  `completed_date` date DEFAULT NULL COMMENT '完成日期',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='心愿清单表';

-- ----------------------------
-- Table: calendar_note (日历月标记表)
-- ----------------------------
DROP TABLE IF EXISTS `calendar_note`;
CREATE TABLE `calendar_note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `note_date` date NOT NULL COMMENT '标记日期',
  `text` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标记文字',
  `icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '📌' COMMENT '图标',
  `year` int NOT NULL COMMENT '年份',
  `month` int NOT NULL COMMENT '月份',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日历月标记表';

-- ----------------------------
-- Table: calendar_mood (日历每日心情表)
-- ----------------------------
DROP TABLE IF EXISTS `calendar_mood`;
CREATE TABLE `calendar_mood` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mood_date` date NOT NULL COMMENT '心情日期',
  `mood` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '心情：开心/伤心/普通/生气',
  `mood_icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '心情图标',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_date_user`(`mood_date` ASC, `user_id` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日历每日心情表';

-- ----------------------------
-- Table: memory_album (回忆相册表)
-- ----------------------------
DROP TABLE IF EXISTS `memory_album`;
CREATE TABLE `memory_album` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地点',
  `album_date` date DEFAULT NULL COMMENT '日期',
  `emoji` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '表情图标',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图片URL',
  `photo_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '相册内图片URL列表，逗号分隔',
  `is_private` tinyint(1) DEFAULT 1 COMMENT '是否私密 0-否 1-是',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回忆相册表';

-- ----------------------------
-- Table: memory_moment (记忆瞬间表)
-- ----------------------------
DROP TABLE IF EXISTS `memory_moment`;
CREATE TABLE `memory_moment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `moment_date` date NOT NULL COMMENT '日期',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地点',
  `emoji` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '表情图标',
  `year` int NOT NULL COMMENT '年份（冗余，便于分组查询）',
  `month` int NOT NULL COMMENT '月份（冗余，便于分组查询）',
  `photo_urls` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '照片URL列表，逗号分隔',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='记忆瞬间表';

-- ----------------------------
-- Table: visited_location (足迹地点表)
-- ----------------------------
DROP TABLE IF EXISTS `visited_location`;
CREATE TABLE `visited_location` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市名',
  `province` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份',
  `visit_date` date DEFAULT NULL COMMENT '到访日期',
  `map_x` decimal(5, 2) DEFAULT NULL COMMENT '地图X坐标百分比（旧）',
  `map_y` decimal(5, 2) DEFAULT NULL COMMENT '地图Y坐标百分比（旧）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题描述',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '照片URL',
  `lat` decimal(10, 7) DEFAULT NULL COMMENT '纬度',
  `lng` decimal(10, 7) DEFAULT NULL COMMENT '经度',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='足迹地点表';

-- ----------------------------
-- Table: custom_emoji (自定义表情表 - 共享)
-- ----------------------------
DROP TABLE IF EXISTS `custom_emoji`;
CREATE TABLE `custom_emoji` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `emoji_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '表情文字（如 🤓）',
  `emoji_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '表情名称',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_emoji_url`(`emoji_url` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自定义表情表（共享）';

SET FOREIGN_KEY_CHECKS = 1;
