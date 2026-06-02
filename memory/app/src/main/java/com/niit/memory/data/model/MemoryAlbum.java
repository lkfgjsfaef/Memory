package com.niit.memory.data.model;

import com.google.gson.annotations.SerializedName;

public class MemoryAlbum {
    private Long id;
    private String location;
    @SerializedName("albumDate")
    private String albumDate;
    private String emoji;
    private String coverUrl;
    private String photoUrls;
    private Integer isPrivate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getAlbumDate() { return albumDate; }
    public void setAlbumDate(String albumDate) { this.albumDate = albumDate; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
    public Integer getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Integer isPrivate) { this.isPrivate = isPrivate; }
}
