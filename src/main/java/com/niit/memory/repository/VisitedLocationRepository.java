package com.niit.memory.repository;

import com.niit.memory.entity.VisitedLocation;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class VisitedLocationRepository {

    private final JdbcTemplate jdbcTemplate;

    public VisitedLocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<VisitedLocation> findAll() {
        String sql = "SELECT * FROM visited_location ORDER BY visit_date DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(VisitedLocation.class));
    }

    public Long insert(VisitedLocation loc) {
        String sql = "INSERT INTO visited_location (name, province, visit_date, map_x, map_y, title, image_url, lat, lng) VALUES (?,?,?,?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, loc.getName());
            ps.setString(2, loc.getProvince());
            ps.setObject(3, loc.getVisitDate());
            ps.setBigDecimal(4, loc.getMapX());
            ps.setBigDecimal(5, loc.getMapY());
            ps.setString(6, loc.getTitle());
            ps.setString(7, loc.getImageUrl());
            ps.setBigDecimal(8, loc.getLat());
            ps.setBigDecimal(9, loc.getLng());
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int update(VisitedLocation loc) {
        String sql = "UPDATE visited_location SET name=?, province=?, visit_date=?, map_x=?, map_y=?, title=?, image_url=?, lat=?, lng=? WHERE id=?";
        return jdbcTemplate.update(sql, loc.getName(), loc.getProvince(), loc.getVisitDate(),
                loc.getMapX(), loc.getMapY(), loc.getTitle(), loc.getImageUrl(),
                loc.getLat(), loc.getLng(), loc.getId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM visited_location WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM visited_location", Integer.class);
    }
}
