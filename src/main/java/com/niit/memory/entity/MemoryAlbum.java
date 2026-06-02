package com.niit.memory.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MemoryAlbum {
    private Long id;
    private String location;
    private LocalDate albumDate;
    private String emoji;
    private String coverUrl;
    private String photoUrls;
    private Integer isPrivate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDate getAlbumDate() { return albumDate; }
    public void setAlbumDate(LocalDate albumDate) { this.albumDate = albumDate; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
    public Integer getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Integer isPrivate) { this.isPrivate = isPrivate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
