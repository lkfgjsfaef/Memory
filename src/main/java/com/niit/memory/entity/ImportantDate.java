package com.niit.memory.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ImportantDate {
    private Long id;
    private String title;
    private String icon;
    private LocalDate eventDate;
    private String lunarDate;
    private String note;
    private Integer recurring;
    private Integer recurringMonth;
    private Integer recurringDay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
