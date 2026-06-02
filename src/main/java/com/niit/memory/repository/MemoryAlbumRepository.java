package com.niit.memory.repository;

import com.niit.memory.entity.MemoryAlbum;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MemoryAlbumRepository {

    private final JdbcTemplate jdbcTemplate;

    public MemoryAlbumRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MemoryAlbum> findAll() {
        String sql = "SELECT * FROM memory_album ORDER BY album_date DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MemoryAlbum.class));
    }

    public MemoryAlbum findById(Long id) {
        String sql = "SELECT * FROM memory_album WHERE id = ?";
        List<MemoryAlbum> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MemoryAlbum.class), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(MemoryAlbum a) {
        String sql = "INSERT INTO memory_album (location, album_date, emoji, cover_url, photo_urls, is_private) VALUES (?,?,?,?,?,?)";
        return jdbcTemplate.update(sql, a.getLocation(), a.getAlbumDate(), a.getEmoji(),
                a.getCoverUrl(), a.getPhotoUrls(), a.getIsPrivate());
    }

    public int update(MemoryAlbum a) {
        String sql = "UPDATE memory_album SET location=?, album_date=?, emoji=?, cover_url=?, photo_urls=?, is_private=? WHERE id=?";
        return jdbcTemplate.update(sql, a.getLocation(), a.getAlbumDate(), a.getEmoji(),
                a.getCoverUrl(), a.getPhotoUrls(), a.getIsPrivate(), a.getId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM memory_album WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
