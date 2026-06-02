package com.niit.memory.data.model;

import com.google.gson.annotations.SerializedName;

public class DailyRecord implements java.io.Serializable {
    private Long id;
    private String title;
    private String content;
    private String author;
    private Long userId;
    private String location;
    private String mood;
    private String moodIcon;
    @SerializedName("recordDate")
    private String recordDate;
    private String imageUrls;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    public String getMoodIcon() { return moodIcon; }
    public void setMoodIcon(String moodIcon) { this.moodIcon = moodIcon; }
    public String getRecordDate() { return recordDate; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
}
