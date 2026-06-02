package com.niit.memory.repository;

import com.niit.memory.entity.CalendarNote;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CalendarNoteRepository {

    private final JdbcTemplate jdbcTemplate;

    public CalendarNoteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CalendarNote> findByYearAndMonth(int year, int month) {
        String sql = "SELECT * FROM calendar_note WHERE year=? AND month=? ORDER BY note_date";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CalendarNote.class), year, month);
    }

    public CalendarNote findById(Long id) {
        String sql = "SELECT * FROM calendar_note WHERE id = ?";
        List<CalendarNote> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CalendarNote.class), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(CalendarNote n) {
        String sql = "INSERT INTO calendar_note (note_date, text, icon, year, month, user_id) VALUES (?,?,?,?,?,?)";
        return jdbcTemplate.update(sql, n.getNoteDate(), n.getText(), n.getIcon(), n.getYear(), n.getMonth(), n.getUserId());
    }

    public int update(CalendarNote n) {
        String sql = "UPDATE calendar_note SET note_date=?, text=?, icon=?, year=?, month=? WHERE id=?";
        return jdbcTemplate.update(sql, n.getNoteDate(), n.getText(), n.getIcon(),
                n.getYear(), n.getMonth(), n.getId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM calendar_note WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
