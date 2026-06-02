package com.niit.memory.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Couple {
    private Long id;
    private String hisName;
    private String herName;
    private LocalDate loveStartDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHisName() { return hisName; }
    public void setHisName(String hisName) { this.hisName = hisName; }
    public String getHerName() { return herName; }
    public void setHerName(String herName) { this.herName = herName; }
    public LocalDate getLoveStartDate() { return loveStartDate; }
    public void setLoveStartDate(LocalDate loveStartDate) { this.loveStartDate = loveStartDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
