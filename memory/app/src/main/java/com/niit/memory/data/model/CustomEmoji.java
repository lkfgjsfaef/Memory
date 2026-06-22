package com.niit.memory.data.model;

public class CustomEmoji {
    private Long id;
    private Long userId;
    private String emojiUrl;
    private String emojiName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmojiUrl() { return emojiUrl; }
    public void setEmojiUrl(String emojiUrl) { this.emojiUrl = emojiUrl; }
    public String getEmojiName() { return emojiName; }
    public void setEmojiName(String emojiName) { this.emojiName = emojiName; }
}
