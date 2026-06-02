package com.niit.memory.repository;

import com.niit.memory.entity.Wish;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class WishRepository {

    private final JdbcTemplate jdbcTemplate;

    public WishRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Wish> findAll(String status, String category) {
        StringBuilder sql = new StringBuilder("SELECT * FROM wish WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (category != null && !category.isEmpty() && !"all".equals(category)) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        sql.append(" ORDER BY created_at DESC");
        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(Wish.class), params.toArray());
    }

    public Wish findById(Long id) {
        String sql = "SELECT * FROM wish WHERE id = ?";
        List<Wish> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Wish.class), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(Wish w) {
        String sql = "INSERT INTO wish (title, description, category, status, author, user_id, image_urls, start_date) VALUES (?,?,?,?,?,?,?,?)";
        return jdbcTemplate.update(sql, w.getTitle(), w.getDescription(), w.getCategory(),
                w.getStatus(), w.getAuthor(), w.getUserId(), w.getImageUrls(), w.getStartDate());
    }

    public int update(Wish w) {
        String sql = "UPDATE wish SET title=?, description=?, category=?, status=?, author=?, image_urls=?, start_date=?, completed_date=? WHERE id=?";
        return jdbcTemplate.update(sql, w.getTitle(), w.getDescription(), w.getCategory(),
                w.getStatus(), w.getAuthor(), w.getImageUrls(), w.getStartDate(), w.getCompletedDate(), w.getId());
    }

    public int updateStatus(Long id, String status) {
        String sql = "UPDATE wish SET status=? WHERE id=?";
        return jdbcTemplate.update(sql, status, id);
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM wish WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int countAll() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wish", Integer.class);
    }

    public int countByStatus(String status) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wish WHERE status=?", Integer.class, status);
    }
}
