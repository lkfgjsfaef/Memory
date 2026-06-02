package com.niit.memory.repository;

import com.niit.memory.entity.CalendarMood;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CalendarMoodRepository {

    private final JdbcTemplate jdbc;

    public CalendarMoodRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CalendarMood> findByYearAndMonth(int year, int month) {
        String sql = "SELECT * FROM calendar_mood WHERE YEAR(mood_date)=? AND MONTH(mood_date)=?";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(CalendarMood.class), year, month);
    }

    public CalendarMood findByDate(String dateStr) {
        String sql = "SELECT * FROM calendar_mood WHERE mood_date=?";
        List<CalendarMood> list = jdbc.query(sql, new BeanPropertyRowMapper<>(CalendarMood.class), dateStr);
        return list.isEmpty() ? null : list.get(0);
    }

    public int upsert(CalendarMood mood) {
        String sql = "INSERT INTO calendar_mood (mood_date, mood, mood_icon, user_id) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE mood=VALUES(mood), mood_icon=VALUES(mood_icon)";
        return jdbc.update(sql, mood.getMoodDate().toString(), mood.getMood(), mood.getMoodIcon(), mood.getUserId());
    }
}
