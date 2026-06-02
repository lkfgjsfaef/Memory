/*
 Navicat Premium Dump SQL

 Source Server         : 2023mysql
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : memory_db

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 01/06/2026 13:58:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for calendar_mood
-- ----------------------------
DROP TABLE IF EXISTS `calendar_mood`;
CREATE TABLE `calendar_mood`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mood_date` date NOT NULL COMMENT '心情日期',
  `mood` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '心情：开心/伤心/普通/生气',
  `mood_icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '心情图标',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联用户',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_date_user`(`mood_date` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日历每日心情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of calendar_mood
-- ----------------------------
INSERT INTO `calendar_mood` VALUES (1, '2026-05-27', '开心', '😊', 2, '2026-05-27 17:12:05', '2026-05-27 17:12:05');
INSERT INTO `calendar_mood` VALUES (2, '2026-05-27', '开心', '😊', 1, '2026-05-27 17:12:18', '2026-05-27 17:12:18');
INSERT INTO `calendar_mood` VALUES (3, '2026-05-29', '幸福', '🥰', 1, '2026-05-29 19:09:40', '2026-05-29 20:23:31');
INSERT INTO `calendar_mood` VALUES (22, '2026-05-30', '开心', '😄', 1, '2026-05-30 15:45:54', '2026-05-30 15:51:17');
INSERT INTO `calendar_mood` VALUES (26, '2026-05-30', '开心', '😄', 2, '2026-05-30 17:51:58', '2026-05-30 18:17:45');
INSERT INTO `calendar_mood` VALUES (33, '2026-05-31', '开心', '😊', 1, '2026-05-31 14:27:51', '2026-05-31 18:41:42');
INSERT INTO `calendar_mood` VALUES (48, '2026-06-01', '开心', '😊', 1, '2026-06-01 11:51:09', '2026-06-01 11:51:09');
INSERT INTO `calendar_mood` VALUES (50, '2026-06-02', '开心', '😊', 1, '2026-06-01 12:47:32', '2026-06-01 12:47:32');

-- ----------------------------
-- Table structure for calendar_note
-- ----------------------------
DROP TABLE IF EXISTS `calendar_note`;
CREATE TABLE `calendar_note`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `note_date` date NOT NULL COMMENT '标记日期',
  `text` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标记文字',
  `icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '?' COMMENT '图标',
  `year` int NOT NULL COMMENT '年份',
  `month` int NOT NULL COMMENT '月份',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联用户',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日历月标记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of calendar_note
-- ----------------------------

-- ----------------------------
-- Table structure for couple
-- ----------------------------
DROP TABLE IF EXISTS `couple`;
CREATE TABLE `couple`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `his_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '他的名字',
  `her_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '她的名字',
  `love_start_date` date NOT NULL COMMENT '恋爱开始日期',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '情侣信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of couple
-- ----------------------------
INSERT INTO `couple` VALUES (1, '酱酱', '菲菲', '2026-03-16', '2026-05-27 15:11:56', '2026-05-27 16:03:30');

-- ----------------------------
-- Table structure for daily_record
-- ----------------------------
DROP TABLE IF EXISTS `daily_record`;
CREATE TABLE `daily_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '正文内容',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作者',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联用户',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地点',
  `mood` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '心情',
  `mood_icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '心情图标',
  `record_date` date NOT NULL COMMENT '记录日期',
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片URL列表，逗号分隔',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日常记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of daily_record
-- ----------------------------
INSERT INTO `daily_record` VALUES (4, '吃桃子🍑', '今天中午吃了三个桃子🍑，中午好热，村里很多来玩的，桃子树结了很多😋', '她', 2, '', '开心', '😊', '2026-05-30', 'http://www.voyageart.top/lo-pJ85nlzrDc5Y3jHWVxJzKCGw8,http://www.voyageart.top/lr-GmKx8hEf_CIuc9YGMyVbJ36f-', '2026-05-30 17:50:30', '2026-05-31 14:26:14');
INSERT INTO `daily_record` VALUES (5, '躺平的周末', '今天大部分都在床上躺着，外面起风下小雨🌧️，吃了辣椒酱拌粉😋，可恶，后面的时间好紧好多事情啊啊啊，这就是长大的烦恼吗……', '她', 1, '', '想他', '🥺', '2026-05-31', 'http://www.voyageart.top/FhXXxzY3gYBrvt5xi93d3VnJzaEc,http://www.voyageart.top/lt6sXvUUmLw0A9ZXF7fY8J8rSkKu', '2026-05-31 22:54:33', '2026-06-01 11:50:35');

-- ----------------------------
-- Table structure for important_date
-- ----------------------------
DROP TABLE IF EXISTS `important_date`;
CREATE TABLE `important_date`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `icon` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '?' COMMENT '图标emoji',
  `event_date` date NULL DEFAULT NULL COMMENT '具体日期（公历）',
  `lunar_date` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '农历日期描述',
  `note` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `recurring` tinyint(1) NULL DEFAULT 0 COMMENT '是否每年重复 0-否 1-是',
  `recurring_month` int NULL DEFAULT NULL COMMENT '重复月份（1-12）',
  `recurring_day` int NULL DEFAULT NULL COMMENT '重复日期（1-31）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '重要日子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of important_date
-- ----------------------------
INSERT INTO `important_date` VALUES (19, '100天纪念日', '💯', '2026-06-24', NULL, '在一起100天啦！', 0, NULL, NULL, '2026-05-27 16:03:30', '2026-05-27 16:03:30');
INSERT INTO `important_date` VALUES (20, '酱酱的生日', '🎂', '2026-03-08', '', '酱酱的生日', 1, NULL, NULL, '2026-05-27 16:03:30', '2026-05-27 17:06:07');
INSERT INTO `important_date` VALUES (21, '菲菲的生日', '🎂', '2026-06-30', '', '菲菲的生日', 1, NULL, NULL, '2026-05-27 16:03:30', '2026-05-27 17:05:59');
INSERT INTO `important_date` VALUES (22, '元旦', '🎌', '2026-01-01', NULL, '节假日', 1, 1, 1, '2026-05-27 16:03:31', '2026-05-27 16:03:31');
INSERT INTO `important_date` VALUES (23, '春节', '🧧', '2026-02-17', NULL, '节假日', 0, NULL, NULL, '2026-05-27 16:03:31', '2026-05-27 16:03:31');
INSERT INTO `important_date` VALUES (24, '元宵节', '🏮', '2026-03-03', NULL, '节假日', 0, NULL, NULL, '2026-05-27 16:03:31', '2026-05-27 16:03:31');
INSERT INTO `important_date` VALUES (25, '清明节', '🌿', '2026-04-05', NULL, '节假日', 1, 4, 5, '2026-05-27 16:03:31', '2026-05-27 16:03:31');
INSERT INTO `important_date` VALUES (26, '劳动节', '💪', '2026-05-01', NULL, '节假日', 1, 5, 1, '2026-05-27 16:03:31', '2026-05-27 16:03:31');
INSERT INTO `important_date` VALUES (27, '端午节', '🐉', '2026-06-19', NULL, '节假日', 0, NULL, NULL, '2026-05-27 16:03:31', '2026-05-27 16:03:31');
INSERT INTO `important_date` VALUES (28, '七夕节', '🎋', '2026-08-19', NULL, '节假日', 0, NULL, NULL, '2026-05-27 16:03:31', '2026-05-27 16:03:31');
INSERT INTO `important_date` VALUES (29, '中秋节', '🌕', '2026-09-25', NULL, '节假日', 0, NULL, NULL, '2026-05-27 16:03:31', '2026-05-27 16:03:31');
INSERT INTO `important_date` VALUES (30, '国庆节', '🇨🇳', '2026-10-01', NULL, '节假日', 1, 10, 1, '2026-05-27 16:03:31', '2026-05-27 16:03:31');
INSERT INTO `important_date` VALUES (31, '六一儿童节💙', '💚', '2026-06-01', '', '怎么能不过呢🤪🥳', 1, NULL, NULL, '2026-05-31 22:50:08', '2026-05-31 22:50:08');

-- ----------------------------
-- Table structure for memory_album
-- ----------------------------
DROP TABLE IF EXISTS `memory_album`;
CREATE TABLE `memory_album`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地点',
  `album_date` date NULL DEFAULT NULL COMMENT '日期',
  `emoji` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '表情图标',
  `cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `photo_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '相册内图片URL列表，逗号分隔',
  `is_private` tinyint(1) NULL DEFAULT 1 COMMENT '是否私密 0-否 1-是',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '回忆相册表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of memory_album
-- ----------------------------
INSERT INTO `memory_album` VALUES (2, '日常生活', '2026-05-18', '🧸', NULL, 'http://www.voyageart.top/Fp_qLJmiSCw0ShkR6ufvtWRwtf2O', 0, '2026-05-27 15:11:56', '2026-05-31 14:41:21');

-- ----------------------------
-- Table structure for memory_moment
-- ----------------------------
DROP TABLE IF EXISTS `memory_moment`;
CREATE TABLE `memory_moment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `moment_date` date NOT NULL COMMENT '日期',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地点',
  `emoji` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '表情图标',
  `year` int NOT NULL COMMENT '年份（冗余，便于分组查询）',
  `month` int NOT NULL COMMENT '月份（冗余，便于分组查询）',
  `photo_urls` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '照片URL列表，逗号分隔',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '记忆瞬间表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of memory_moment
-- ----------------------------
INSERT INTO `memory_moment` VALUES (4, '1', '2026-05-31', '1', '🏞️', 2026, 5, 'http://www.voyageart.top/Fp_qLJmiSCw0ShkR6ufvtWRwtf2O', '2026-05-31 14:38:08', '2026-05-31 16:38:51');

-- ----------------------------
-- Table structure for music_playlist
-- ----------------------------
DROP TABLE IF EXISTS `music_playlist`;
CREATE TABLE `music_playlist`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `song_id` bigint NOT NULL COMMENT '网易云歌曲ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '歌曲名',
  `artist` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '歌手',
  `album` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专辑',
  `duration` bigint NULL DEFAULT NULL COMMENT '时长（毫秒）',
  `pic_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `song_id`(`song_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '音乐歌单表（全局共享）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of music_playlist
-- ----------------------------
INSERT INTO `music_playlist` VALUES (23, 2149062755, 'Every Summertime', 'NIKI', 'Shang-Chi and the Legend of the Ten Rings: The Album', 215686, '', 0, '2026-05-31 22:33:46');
INSERT INTO `music_playlist` VALUES (24, 1374329431, 'Dancing With Your Ghost', 'Sasha Alex Sloan', 'Dancing With Your Ghost', 197787, '', 1, '2026-05-31 22:33:46');
INSERT INTO `music_playlist` VALUES (25, 493752015, '第三人称  (Live)', 'Darren达布希勒图', '中国新歌声第二季 第3期', 269710, '', 2, '2026-05-31 22:33:46');
INSERT INTO `music_playlist` VALUES (26, 1945894789, '晴天 (钢琴版) [原唱: 周杰伦]', '纪钧瀚 (Bryan Chi)', '钢琴放松轻听 流行轻音乐 华语经典', 238098, '', 3, '2026-05-31 22:33:46');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录用户名',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'BCrypt加密密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'jiangjiang', '$2a$10$gRRC05pmS8YWsMJv1.AzTeKfZ.rUDmT7.18j4tZANRYQsFAX7Jw6.', '酱酱', 'http://www.voyageart.top/FibK4mVXAA_I5HSJAXqWLANtOmBy', '2026-05-27 15:11:56');
INSERT INTO `users` VALUES (2, 'feifei', '$2a$10$gRRC05pmS8YWsMJv1.AzTeKfZ.rUDmT7.18j4tZANRYQsFAX7Jw6.', '菲菲', 'http://www.voyageart.top/FlV25ODCgOpzMZdTKLIeUwUM4SBf', '2026-05-27 15:11:56');

-- ----------------------------
-- Table structure for visited_location
-- ----------------------------
DROP TABLE IF EXISTS `visited_location`;
CREATE TABLE `visited_location`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市名',
  `province` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份',
  `visit_date` date NULL DEFAULT NULL COMMENT '到访日期',
  `map_x` decimal(5, 2) NULL DEFAULT NULL COMMENT '地图X坐标百分比（旧）',
  `map_y` decimal(5, 2) NULL DEFAULT NULL COMMENT '地图Y坐标百分比（旧）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题描述',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '照片URL',
  `lat` decimal(10, 7) NULL DEFAULT NULL COMMENT '纬度',
  `lng` decimal(10, 7) NULL DEFAULT NULL COMMENT '经度',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '足迹地点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of visited_location
-- ----------------------------
INSERT INTO `visited_location` VALUES (12, '甘孜藏族自治州', '四川', '2026-04-30', NULL, NULL, '他第一次来我的学校😲', 'http://www.voyageart.top/ltxU3hj4OpeVYZ039iA_syW8GRIE', 30.0930906, 102.1777287, '2026-05-31 22:38:13');
INSERT INTO `visited_location` VALUES (13, '阿拉善沙漠', '宁夏回族自治区', '2026-03-14', NULL, NULL, '第一次一起徒步🤓', 'http://www.voyageart.top/lrL01fbAs-a3V8JzoJVqjExbN4UK', 38.8436909, 105.7042453, '2026-05-31 22:41:29');
INSERT INTO `visited_location` VALUES (15, '银川', '宁夏回族自治区', '2026-03-15', NULL, NULL, '我第一次去他的学校😲', 'http://www.voyageart.top/lvXPPG29JWaHnQadR7F8pjyE8m1f', 38.5020964, 106.1322303, '2026-05-31 22:43:46');

-- ----------------------------
-- Table structure for wish
-- ----------------------------
DROP TABLE IF EXISTS `wish`;
CREATE TABLE `wish`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '心愿标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类（未来规划/旅行计划/生活目标）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态 pending/completed',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发起人',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联用户',
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '心愿配图列表，逗号分隔',
  `start_date` date NULL DEFAULT NULL COMMENT '发起日期',
  `completed_date` date NULL DEFAULT NULL COMMENT '完成日期',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '心愿清单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wish
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
