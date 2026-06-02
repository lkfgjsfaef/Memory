-- =============================================
-- Memory 数据库更新脚本 2026-05-27
-- 1. 新增日历心情表
-- 2. 更新情侣信息为真实数据
-- 3. 重置重要日子（含节日标记）
-- 4. 清除地图足迹
-- =============================================

USE memory_db;

-- ============================================
-- 1. 日历心情表（新增）
-- ============================================
DROP TABLE IF EXISTS calendar_mood;
CREATE TABLE calendar_mood (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mood_date DATE NOT NULL COMMENT '心情日期',
    mood VARCHAR(20) NOT NULL COMMENT '心情：开心/伤心/普通/生气',
    mood_icon VARCHAR(20) NOT NULL COMMENT '心情图标',
    user_id BIGINT COMMENT '关联用户',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date_user (mood_date, user_id)
) COMMENT '日历每日心情表';

-- ============================================
-- 2. 更新情侣信息
-- ============================================
UPDATE couple SET his_name = '酱酱', her_name = '菲菲', love_start_date = '2026-03-16';

-- ============================================
-- 3. 清空并重建重要日子
-- ============================================
DELETE FROM important_date;

-- 个人重要日子
INSERT INTO important_date (title, icon, event_date, note, recurring, recurring_month, recurring_day) VALUES
('100天纪念日', '💯', '2026-06-24', '在一起100天啦！', 0, NULL, NULL),
('他的生日', '🎂', '2026-03-08', '酱酱的生日', 1, 3, 8),
('她的生日', '🎂', '2026-06-30', '菲菲的生日', 1, 6, 30);

-- 节假日（非重要日子，仅标记在日历上）
INSERT INTO important_date (title, icon, event_date, note, recurring, recurring_month, recurring_day) VALUES
('元旦', '🎌', '2026-01-01', '节假日', 1, 1, 1),
('春节', '🧧', '2026-02-17', '节假日', 0, NULL, NULL),
('元宵节', '🏮', '2026-03-03', '节假日', 0, NULL, NULL),
('清明节', '🌿', '2026-04-05', '节假日', 1, 4, 5),
('劳动节', '💪', '2026-05-01', '节假日', 1, 5, 1),
('端午节', '🐉', '2026-06-19', '节假日', 0, NULL, NULL),
('七夕节', '🎋', '2026-08-19', '节假日', 0, NULL, NULL),
('中秋节', '🌕', '2026-09-25', '节假日', 0, NULL, NULL),
('国庆节', '🇨🇳', '2026-10-01', '节假日', 1, 10, 1);

-- ============================================
-- 4. 清空足迹（用户自行标点）
-- ============================================
DELETE FROM visited_location;
