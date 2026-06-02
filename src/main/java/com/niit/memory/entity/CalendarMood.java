package com.niit.memory.entity;

import java.time.LocalDate;

public class CalendarMood {
    private Long id;
    private LocalDate moodDate;
    private String mood;
    private String moodIcon;
    private Long userId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getMoodDate() { return moodDate; }
    public void setMoodDate(LocalDate moodDate) { this.moodDate = moodDate; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public String getMoodIcon() { return moodIcon; }
    public void setMoodIcon(String moodIcon) { this.moodIcon = moodIcon; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
