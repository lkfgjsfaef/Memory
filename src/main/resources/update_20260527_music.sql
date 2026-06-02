-- =============================================
-- 2026-05-27: 音乐歌单表（全局共享）
-- =============================================
USE memory_db;

DROP TABLE IF EXISTS music_playlist;
CREATE TABLE music_playlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    song_id BIGINT NOT NULL UNIQUE COMMENT '网易云歌曲ID',
    name VARCHAR(200) COMMENT '歌曲名',
    artist VARCHAR(200) COMMENT '歌手',
    album VARCHAR(200) COMMENT '专辑',
    duration BIGINT COMMENT '时长（毫秒）',
    pic_url VARCHAR(500) COMMENT '封面图URL',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '音乐歌单表（全局共享）';
