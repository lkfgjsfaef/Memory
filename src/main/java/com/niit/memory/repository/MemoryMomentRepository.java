package com.niit.memory.repository;

import com.niit.memory.entity.MemoryMoment;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MemoryMomentRepository {

    private final JdbcTemplate jdbcTemplate;

    public MemoryMomentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MemoryMoment> findAll() {
        String sql = "SELECT * FROM memory_moment ORDER BY moment_date DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MemoryMoment.class));
    }

    public MemoryMoment findById(Long id) {
        String sql = "SELECT * FROM memory_moment WHERE id = ?";
        List<MemoryMoment> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MemoryMoment.class), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(MemoryMoment m) {
        String sql = "INSERT INTO memory_moment (title, moment_date, location, emoji, year, month, photo_urls) VALUES (?,?,?,?,?,?,?)";
        return jdbcTemplate.update(sql, m.getTitle(), m.getMomentDate(), m.getLocation(),
                m.getEmoji(), m.getYear(), m.getMonth(), m.getPhotoUrls());
    }

    public int update(MemoryMoment m) {
        String sql = "UPDATE memory_moment SET title=?, moment_date=?, location=?, emoji=?, year=?, month=?, photo_urls=? WHERE id=?";
        return jdbcTemplate.update(sql, m.getTitle(), m.getMomentDate(), m.getLocation(),
                m.getEmoji(), m.getYear(), m.getMonth(), m.getPhotoUrls(), m.getId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM memory_moment WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
