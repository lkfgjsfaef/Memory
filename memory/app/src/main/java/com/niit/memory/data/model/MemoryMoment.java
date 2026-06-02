package com.niit.memory.data.model;

import com.google.gson.annotations.SerializedName;

public class MemoryMoment {
    private Long id;
    private String title;
    @SerializedName("momentDate")
    private String momentDate;
    private String location;
    private String emoji;
    private Integer year;
    private Integer month;
    private String photoUrls;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMomentDate() { return momentDate; }
    public void setMomentDate(String momentDate) { this.momentDate = momentDate; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
}
