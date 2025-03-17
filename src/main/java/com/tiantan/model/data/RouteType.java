package com.tiantan.model.data;

/**
 * 路线类型枚举
 */
public enum RouteType {
    CLASSIC("经典路线", "Classic Route"),
    CULTURAL("文化体验", "Cultural Experience"),
    PHOTOGRAPHY("摄影精选", "Photography Highlights"),
    QUICK_TOUR("快速游览", "Quick Tour"),
    ACCESSIBLE("无障碍路线", "Accessible Route"),
    HISTORICAL("历史探索", "Historical Exploration");
    
    private final String nameZh;
    private final String nameEn;
    
    RouteType(String nameZh, String nameEn) {
        this.nameZh = nameZh;
        this.nameEn = nameEn;
    }
    
    public String getNameZh() {
        return nameZh;
    }
    
    public String getNameEn() {
        return nameEn;
    }
    
    public String getName(boolean isEnglish) {
        return isEnglish ? nameEn : nameZh;
    }
}