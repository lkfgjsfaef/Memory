package com.niit.memory.data.model;

import com.google.gson.annotations.SerializedName;

public class CalendarNote {
    private Long id;
    @SerializedName("noteDate")
    private String noteDate;
    private String text;
    private String icon;
    private Integer year;
    private Integer month;
    private Long userId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNoteDate() { return noteDate; }
    public void setNoteDate(String noteDate) { this.noteDate = noteDate; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
