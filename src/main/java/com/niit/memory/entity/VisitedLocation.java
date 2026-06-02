package com.niit.memory.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class VisitedLocation {
    private Long id;
    private String name;
    private String province;
    private LocalDate visitDate;
    private BigDecimal mapX;
    private BigDecimal mapY;
    private String title;
    private String imageUrl;
    private BigDecimal lat;
    private BigDecimal lng;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
    public BigDecimal getMapX() { return mapX; }
    public void setMapX(BigDecimal mapX) { this.mapX = mapX; }
    public BigDecimal getMapY() { return mapY; }
    public void setMapY(BigDecimal mapY) { this.mapY = mapY; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
