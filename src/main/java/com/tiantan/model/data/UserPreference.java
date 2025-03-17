package com.tiantan.model.data;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户偏好类 - 用于个性化推荐
 */
public class UserPreference {
    private boolean isEnglish;               // 是否使用英文
    private Set<String> interestedCategories; // 感兴趣的景点类别
    private Duration timeAvailable;          // 可用时间
    private boolean needAccessible;          // 是否需要无障碍设施
    private int walkingSpeed;                // 步行速度（1-5，1最慢，5最快）
    private boolean avoidCrowds;             // 是否避开拥挤区域
    private int maxPhotoSpots;               // 最大摄影景点数量
    private boolean includeHistory;          // 是否包含历史信息
    private int budgetLevel;                 // 预算等级（1-3，1最低，3最高）
    
    /**
     * 构造函数
     */
    public UserPreference() {
        this.isEnglish = false;
        this.interestedCategories = new HashSet<>();
        this.timeAvailable = Duration.ofHours(3); // 默认3小时
        this.needAccessible = false;
        this.walkingSpeed = 3; // 默认中等速度
        this.avoidCrowds = false;
        this.maxPhotoSpots = 0; // 默认不限制
        this.includeHistory = true;
        this.budgetLevel = 2; // 默认中等预算
    }
    
    /**
     * 全参数构造函数
     */
    public UserPreference(boolean isEnglish, Set<String> interestedCategories, Duration timeAvailable,
                         boolean needAccessible, int walkingSpeed, boolean avoidCrowds,
                         int maxPhotoSpots, boolean includeHistory, int budgetLevel) {
        this.isEnglish = isEnglish;
        this.interestedCategories = new HashSet<>(interestedCategories);
        this.timeAvailable = timeAvailable;
        this.needAccessible = needAccessible;
        this.walkingSpeed = walkingSpeed;
        this.avoidCrowds = avoidCrowds;
        this.maxPhotoSpots = maxPhotoSpots;
        this.includeHistory = includeHistory;
        this.budgetLevel = budgetLevel;
    }
    
    // Getters and Setters
    public boolean isEnglish() {
        return isEnglish;
    }
    
    public void setEnglish(boolean english) {
        isEnglish = english;
    }
    
    public Set<String> getInterestedCategories() {
        return new HashSet<>(interestedCategories);
    }
    
    public void setInterestedCategories(Set<String> interestedCategories) {
        this.interestedCategories = new HashSet<>(interestedCategories);
    }
    
    public void addInterestedCategory(String category) {
        this.interestedCategories.add(category);
    }
    
    public void removeInterestedCategory(String category) {
        this.interestedCategories.remove(category);
    }
    
    public Duration getTimeAvailable() {
        return timeAvailable;
    }
    
    public void setTimeAvailable(Duration timeAvailable) {
        this.timeAvailable = timeAvailable;
    }
    
    public boolean isNeedAccessible() {
        return needAccessible;
    }
    
    public void setNeedAccessible(boolean needAccessible) {
        this.needAccessible = needAccessible;
    }
    
    public int getWalkingSpeed() {
        return walkingSpeed;
    }
    
    public void setWalkingSpeed(int walkingSpeed) {
        if (walkingSpeed < 1 || walkingSpeed > 5) {
            throw new IllegalArgumentException("步行速度必须在1到5之间");
        }
        this.walkingSpeed = walkingSpeed;
    }
    
    public boolean isAvoidCrowds() {
        return avoidCrowds;
    }
    
    public void setAvoidCrowds(boolean avoidCrowds) {
        this.avoidCrowds = avoidCrowds;
    }
    
    public int getMaxPhotoSpots() {
        return maxPhotoSpots;
    }
    
    public void setMaxPhotoSpots(int maxPhotoSpots) {
        this.maxPhotoSpots = maxPhotoSpots;
    }
    
    public boolean isIncludeHistory() {
        return includeHistory;
    }
    
    public void setIncludeHistory(boolean includeHistory) {
        this.includeHistory = includeHistory;
    }
    
    public int getBudgetLevel() {
        return budgetLevel;
    }
    
    public void setBudgetLevel(int budgetLevel) {
        if (budgetLevel < 1 || budgetLevel > 3) {
            throw new IllegalArgumentException("预算等级必须在1到3之间");
        }
        this.budgetLevel = budgetLevel;
    }
    
    /**
     * 基于用户偏好计算景点评分
     * @param spot 景点
     * @return 评分（0-100）
     */
    public int calculateSpotScore(ScenicSpot spot) {
        int score = 50; // 基础分数
        
        // 类别匹配加分
        if (interestedCategories.contains(spot.getCategory())) {
            score += 20;
        }
        
        // 无障碍设施匹配
        if (needAccessible && !spot.isAccessible()) {
            score -= 40; // 严重降低不符合无障碍要求的景点分数
        }
        
        // 热门程度影响
        if (avoidCrowds) {
            // 避开拥挤区域时，热门程度越高，分数越低
            score -= (spot.getPopularity() / 5);
        } else {
            // 否则热门景点略微加分
            score += (spot.getPopularity() / 10);
        }
        
        // 预算考虑
        if (budgetLevel == 1 && spot.getEntranceFee() > 50) {
            score -= 15; // 低预算用户对高价景点降分
        } else if (budgetLevel == 3 && spot.getEntranceFee() > 80) {
            score += 10; // 高预算用户对高价景点加分
        }
        
        // 确保分数在0-100范围内
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * 基于用户偏好计算路线评分
     * @param route 路线
     * @return 评分（0-100）
     */
    public int calculateRouteScore(Route route) {
        int score = 50; // 基础分数
        
        // 时间匹配度
        long routeMinutes = route.getEstimatedDuration().toMinutes();
        long availableMinutes = timeAvailable.toMinutes();
        
        // 如果路线时间超过可用时间的110%，大幅降分
        if (routeMinutes > availableMinutes * 1.1) {
            score -= 30;
        } 
        // 如果路线时间在可用时间的90%-110%之间，最佳匹配
        else if (routeMinutes >= availableMinutes * 0.9 && routeMinutes <= availableMinutes * 1.1) {
            score += 20;
        }
        // 如果路线时间不足可用时间的70%，稍微降分
        else if (routeMinutes < availableMinutes * 0.7) {
            score -= 10;
        }
        
        // 无障碍需求匹配
        if (needAccessible && !route.isAccessible()) {
            score -= 50; // 严重降低不符合无障碍要求的路线分数
        }
        
        // 路线类型匹配
        if (avoidCrowds && route.getType() == RouteType.QUICK_TOUR) {
            score += 15;
        }
        
        if (maxPhotoSpots > 0 && route.getType() == RouteType.PHOTOGRAPHY) {
            score += 15;
        }
        
        if (includeHistory && route.getType() == RouteType.HISTORICAL) {
            score += 15;
        }
        
        // 确保分数在0-100范围内
        return Math.max(0, Math.min(100, score));
    }
}