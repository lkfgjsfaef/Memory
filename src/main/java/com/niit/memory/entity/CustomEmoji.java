package com.niit.memory.entity;

import java.time.LocalDateTime;

public class CustomEmoji {
    private Long id;
    private String emojiUrl;
    private String emojiName;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmojiUrl() { return emojiUrl; }
    public void setEmojiUrl(String emojiUrl) { this.emojiUrl = emojiUrl; }
    public String getEmojiName() { return emojiName; }
    public void setEmojiName(String emojiName) { this.emojiName = emojiName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
