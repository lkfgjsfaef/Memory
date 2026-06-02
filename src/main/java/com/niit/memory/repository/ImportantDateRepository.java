package com.niit.memory.repository;

import com.niit.memory.entity.ImportantDate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ImportantDateRepository {

    private final JdbcTemplate jdbcTemplate;

    public ImportantDateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ImportantDate> findAll() {
        String sql = "SELECT * FROM important_date ORDER BY event_date";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ImportantDate.class));
    }

    public ImportantDate findById(Long id) {
        String sql = "SELECT * FROM important_date WHERE id = ?";
        List<ImportantDate> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ImportantDate.class), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(ImportantDate d) {
        String sql = "INSERT INTO important_date (title, icon, event_date, lunar_date, note, recurring, recurring_month, recurring_day) VALUES (?,?,?,?,?,?,?,?)";
        return jdbcTemplate.update(sql, d.getTitle(), d.getIcon(), d.getEventDate(),
                d.getLunarDate(), d.getNote(), d.getRecurring(),
                d.getRecurringMonth(), d.getRecurringDay());
    }

    public int update(ImportantDate d) {
        String sql = "UPDATE important_date SET title=?, icon=?, event_date=?, lunar_date=?, note=?, recurring=?, recurring_month=?, recurring_day=? WHERE id=?";
        return jdbcTemplate.update(sql, d.getTitle(), d.getIcon(), d.getEventDate(),
                d.getLunarDate(), d.getNote(), d.getRecurring(),
                d.getRecurringMonth(), d.getRecurringDay(), d.getId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM important_date WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
