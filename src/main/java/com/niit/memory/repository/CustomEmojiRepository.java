package com.niit.memory.repository;

import com.niit.memory.entity.CustomEmoji;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomEmojiRepository {

    private final JdbcTemplate jdbc;

    public CustomEmojiRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CustomEmoji> findAll() {
        return jdbc.query(
            "SELECT id, emoji_url AS emojiUrl, emoji_name AS emojiName, created_at AS createdAt FROM custom_emoji ORDER BY created_at DESC",
            new BeanPropertyRowMapper<>(CustomEmoji.class));
    }

    public int insert(CustomEmoji emoji) {
        return jdbc.update(
            "INSERT INTO custom_emoji (emoji_url, emoji_name) VALUES (?, ?)",
            emoji.getEmojiUrl(), emoji.getEmojiName());
    }

    public boolean existsByEmojiUrl(String emojiUrl) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM custom_emoji WHERE emoji_url = ?", Integer.class, emojiUrl);
        return count != null && count > 0;
    }

    public int deleteById(Long id) {
        return jdbc.update("DELETE FROM custom_emoji WHERE id = ?", id);
    }
}
