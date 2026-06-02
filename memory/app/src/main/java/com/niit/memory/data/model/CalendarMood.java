package com.niit.memory.data.model;

import com.google.gson.annotations.SerializedName;

public class CalendarMood {
    private Long id;
    @SerializedName("moodDate")
    private String moodDate;
    private String mood;
    private String moodIcon;
    private Long userId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMoodDate() { return moodDate; }
    public void setMoodDate(String moodDate) { this.moodDate = moodDate; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public String getMoodIcon() { return moodIcon; }
    public void setMoodIcon(String moodIcon) { this.moodIcon = moodIcon; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
