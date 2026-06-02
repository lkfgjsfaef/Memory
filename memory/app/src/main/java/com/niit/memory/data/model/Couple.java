package com.niit.memory.data.model;

import com.google.gson.annotations.SerializedName;

public class Couple {
    private Long id;
    private String hisName;
    private String herName;
    @SerializedName("loveStartDate")
    private String loveStartDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHisName() { return hisName; }
    public void setHisName(String hisName) { this.hisName = hisName; }
    public String getHerName() { return herName; }
    public void setHerName(String herName) { this.herName = herName; }
    public String getLoveStartDate() { return loveStartDate; }
    public void setLoveStartDate(String loveStartDate) { this.loveStartDate = loveStartDate; }
}
