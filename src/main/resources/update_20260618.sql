-- ============================================
-- 2026-06-18 综合迁移
--   1. 删除 music_playlist 表（移除音乐播放器功能）
--   2. 新增 custom_emoji 表（日历自定义表情）
-- ============================================

-- 删除音乐歌单表
DROP TABLE IF EXISTS `music_playlist`;

-- 新增自定义表情表（两个账号共通）
-- 如果旧表已存在，先修复为共享模式
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'memory_db' AND TABLE_NAME = 'custom_emoji' AND COLUMN_NAME = 'user_id');
SET @sql_drop_user_id = IF(@col_exists > 0, 'ALTER TABLE `custom_emoji` DROP COLUMN `user_id`', 'SELECT 1');
PREPARE stmt FROM @sql_drop_user_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `custom_emoji` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `emoji_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '表情文字（如 🤓）',
  `emoji_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '表情名称',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_emoji_url`(`emoji_url` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自定义表情表（共享）';

-- 修复已存在的表：清空旧数据，改 emoji_url 排序规则为 utf8mb4_bin，防止不同 emoji 被判重
DELETE FROM `custom_emoji`;
ALTER TABLE `custom_emoji` MODIFY COLUMN `emoji_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '表情文字（如 🤓）';
