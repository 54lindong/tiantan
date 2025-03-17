package com.tiantan.model.data;

import java.time.Duration;

/**
 * 路线停留点类
 */
public class RouteStop {
    private ScenicSpot spot;              // 景点
    private Duration stayDuration;        // 建议停留时间
    private String noteZh;                // 中文注解
    private String noteEn;                // 英文注解
    
    /**
     * 构造函数
     */
    public RouteStop(ScenicSpot spot, Duration stayDuration, String noteZh, String noteEn) {
        this.spot = spot;
        this.stayDuration = stayDuration;
        this.noteZh = noteZh;
        this.noteEn = noteEn;
    }
    
    /**
     * 简化的构造函数，使用景点默认游览时间
     */
    public RouteStop(ScenicSpot spot) {
        this.spot = spot;
        this.stayDuration = Duration.ofMinutes(spot.getVisitTime());
        this.noteZh = "";
        this.noteEn = "";
    }

    // Getters and Setters
    public ScenicSpot getSpot() {
        return spot;
    }

    public void setSpot(ScenicSpot spot) {
        this.spot = spot;
    }

    public Duration getStayDuration() {
        return stayDuration;
    }

    public void setStayDuration(Duration stayDuration) {
        this.stayDuration = stayDuration;
    }

    public String getNoteZh() {
        return noteZh;
    }

    public void setNoteZh(String noteZh) {
        this.noteZh = noteZh;
    }

    public String getNoteEn() {
        return noteEn;
    }

    public void setNoteEn(String noteEn) {
        this.noteEn = noteEn;
    }
    
    /**
     * 根据当前语言获取注解
     * @param isEnglish 是否使用英文
     * @return 注解文本
     */
    public String getNote(boolean isEnglish) {
        return isEnglish ? noteEn : noteZh;
    }
    
    @Override
    public String toString() {
        return "RouteStop{" +
                "spot=" + spot.getNameZh() +
                ", duration=" + stayDuration.toMinutes() + " min" +
                '}';
    }
}