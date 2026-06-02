package com.niit.memory.service;

import com.niit.memory.config.UserContext;
import com.niit.memory.entity.CalendarNote;
import com.niit.memory.repository.CalendarNoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarNoteService {

    private final CalendarNoteRepository repository;

    public CalendarNoteService(CalendarNoteRepository repository) {
        this.repository = repository;
    }

    public List<CalendarNote> findByYearAndMonth(int year, int month) {
        return repository.findByYearAndMonth(year, month);
    }

    public CalendarNote create(CalendarNote note) {
        note.setUserId(UserContext.getUserId());
        if (note.getNoteDate() != null) {
            note.setYear(note.getNoteDate().getYear());
            note.setMonth(note.getNoteDate().getMonthValue());
        }
        repository.insert(note);
        return note;
    }

    public CalendarNote update(CalendarNote note) {
        repository.update(note);
        return repository.findById(note.getId());
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
