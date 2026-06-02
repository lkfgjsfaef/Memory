package com.niit.memory.repository;

import com.niit.memory.entity.DailyRecord;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DailyRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public DailyRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DailyRecord> findAll(Integer year, Integer month) {
        StringBuilder sql = new StringBuilder("SELECT * FROM daily_record WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (year != null) {
            sql.append(" AND YEAR(record_date) = ?");
            params.add(year);
        }
        if (month != null) {
            sql.append(" AND MONTH(record_date) = ?");
            params.add(month);
        }
        sql.append(" ORDER BY record_date DESC");
        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(DailyRecord.class), params.toArray());
    }

    public DailyRecord findById(Long id) {
        String sql = "SELECT * FROM daily_record WHERE id = ?";
        List<DailyRecord> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(DailyRecord.class), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(DailyRecord r) {
        String sql = "INSERT INTO daily_record (title, content, author, user_id, location, mood, mood_icon, record_date, image_urls) VALUES (?,?,?,?,?,?,?,?,?)";
        return jdbcTemplate.update(sql, r.getTitle(), r.getContent(), r.getAuthor(),
                r.getUserId(), r.getLocation(), r.getMood(), r.getMoodIcon(), r.getRecordDate(), r.getImageUrls());
    }

    public int update(DailyRecord r) {
        String sql = "UPDATE daily_record SET title=?, content=?, author=?, location=?, mood=?, mood_icon=?, record_date=?, image_urls=? WHERE id=?";
        return jdbcTemplate.update(sql, r.getTitle(), r.getContent(), r.getAuthor(),
                r.getLocation(), r.getMood(), r.getMoodIcon(), r.getRecordDate(),
                r.getImageUrls(), r.getId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM daily_record WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM daily_record";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public int countByMonth(int year, int month) {
        String sql = "SELECT COUNT(*) FROM daily_record WHERE YEAR(record_date)=? AND MONTH(record_date)=?";
        return jdbcTemplate.queryForObject(sql, Integer.class, year, month);
    }
}
