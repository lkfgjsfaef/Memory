package com.niit.memory.repository;

import com.niit.memory.entity.Couple;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CoupleRepository {

    private final JdbcTemplate jdbcTemplate;

    public CoupleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Couple findById(Long id) {
        String sql = "SELECT * FROM couple WHERE id = ?";
        List<Couple> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Couple.class), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Couple findFirst() {
        String sql = "SELECT * FROM couple ORDER BY id LIMIT 1";
        List<Couple> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Couple.class));
        return list.isEmpty() ? null : list.get(0);
    }

    public int update(Couple couple) {
        String sql = "UPDATE couple SET his_name=?, her_name=?, love_start_date=? WHERE id=?";
        return jdbcTemplate.update(sql, couple.getHisName(), couple.getHerName(),
                couple.getLoveStartDate(), couple.getId());
    }
}
