package com.niit.memory.data.model;

import java.math.BigDecimal;
import com.google.gson.annotations.SerializedName;

public class VisitedLocation {
    private Long id;
    private String name;
    private String province;
    @SerializedName("visitDate")
    private String visitDate;
    private String title;
    private String imageUrl;
    private BigDecimal mapX;
    private BigDecimal mapY;
    private Double lat;
    private Double lng;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public BigDecimal getMapX() { return mapX; }
    public void setMapX(BigDecimal mapX) { this.mapX = mapX; }
    public BigDecimal getMapY() { return mapY; }
    public void setMapY(BigDecimal mapY) { this.mapY = mapY; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
}
