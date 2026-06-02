package com.niit.memory.data.model;

import com.google.gson.annotations.SerializedName;

public class ImportantDate {
    private Long id;
    private String title;
    private String icon;
    @SerializedName("eventDate")
    private String eventDate;
    private String lunarDate;
    private String note;
    private Integer recurring;
    private Integer recurringMonth;
    private Integer recurringDay;
    private Long daysLeft;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getLunarDate() { return lunarDate; }
    public void setLunarDate(String lunarDate) { this.lunarDate = lunarDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Integer getRecurring() { return recurring; }
    public void setRecurring(Integer recurring) { this.recurring = recurring; }
    public Integer getRecurringMonth() { return recurringMonth; }
    public void setRecurringMonth(Integer recurringMonth) { this.recurringMonth = recurringMonth; }
    public Integer getRecurringDay() { return recurringDay; }
    public void setRecurringDay(Integer recurringDay) { this.recurringDay = recurringDay; }
    public Long getDaysLeft() { return daysLeft; }
    public void setDaysLeft(Long daysLeft) { this.daysLeft = daysLeft; }
}
