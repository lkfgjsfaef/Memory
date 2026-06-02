package com.niit.memory.repository;

import com.niit.memory.entity.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), username);
        return users.isEmpty() ? null : users.get(0);
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int updateAvatar(Long id, String avatarUrl) {
        String sql = "UPDATE users SET avatar_url = ? WHERE id = ?";
        return jdbcTemplate.update(sql, avatarUrl, id);
    }
}
