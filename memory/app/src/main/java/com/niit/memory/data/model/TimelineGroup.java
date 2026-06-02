package com.niit.memory.data.model;

import java.util.List;

public class TimelineGroup {
    private String label;
    private Integer year;
    private Integer month;
    private List<MemoryMoment> moments;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public List<MemoryMoment> getMoments() { return moments; }
    public void setMoments(List<MemoryMoment> moments) { this.moments = moments; }
}
