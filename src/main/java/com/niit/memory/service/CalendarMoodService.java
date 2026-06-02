package com.niit.memory.service;

import com.niit.memory.config.UserContext;
import com.niit.memory.entity.CalendarMood;
import com.niit.memory.repository.CalendarMoodRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarMoodService {

    private final CalendarMoodRepository repository;

    public CalendarMoodService(CalendarMoodRepository repository) {
        this.repository = repository;
    }

    public List<CalendarMood> findByYearAndMonth(int year, int month) {
        return repository.findByYearAndMonth(year, month);
    }

    public CalendarMood upsert(CalendarMood mood) {
        if (mood.getMoodDate() == null) {
            throw new IllegalArgumentException("moodDate must not be null");
        }
        mood.setUserId(UserContext.getUserId());
        repository.upsert(mood);
        return mood;
    }
}
