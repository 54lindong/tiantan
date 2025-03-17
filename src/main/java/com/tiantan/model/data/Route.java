package com.tiantan.model.data;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 游览路线类
 */
public class Route {
    private int id;                        // 路线ID
    private String nameZh;                 // 中文名称
    private String nameEn;                 // 英文名称
    private String descriptionZh;          // 中文描述
    private String descriptionEn;          // 英文描述
    private List<RouteStop> stops;         // 路线上的停留点
    private Duration estimatedDuration;    // 预计游览时间
    private double totalDistance;          // 总路程
    private boolean isAccessible;          // 是否无障碍友好
    private RouteType type;                // 路线类型
    private int popularity;                // 热门程度(1-100)

    /**
     * 构造函数
     */
    public Route(int id, String nameZh, String nameEn, String descriptionZh, String descriptionEn,
                RouteType type, boolean isAccessible) {
        this.id = id;
        this.nameZh = nameZh;
        this.nameEn = nameEn;
        this.descriptionZh = descriptionZh;
        this.descriptionEn = descriptionEn;
        this.stops = new ArrayList<>();
        this.estimatedDuration = Duration.ZERO;
        this.totalDistance = 0.0;
        this.isAccessible = isAccessible;
        this.type = type;
        this.popularity = 0;
    }

    /**
     * 添加停留点
     * @param stop 路线停留点
     */
    public void addStop(RouteStop stop) {
        stops.add(stop);
        updateRouteStats();
    }

    /**
     * 在指定位置插入停留点
     * @param index 插入位置
     * @param stop 路线停留点
     */
    public void insertStop(int index, RouteStop stop) {
        if (index < 0 || index > stops.size()) {
            throw new IndexOutOfBoundsException("索引越界: " + index);
        }
        stops.add(index, stop);
        updateRouteStats();
    }

    /**
     * 移除停留点
     * @param index 停留点索引
     * @return 移除的停留点
     */
    public RouteStop removeStop(int index) {
        if (index < 0 || index >= stops.size()) {
            throw new IndexOutOfBoundsException("索引越界: " + index);
        }
        RouteStop removed = stops.remove(index);
        updateRouteStats();
        return removed;
    }

    /**
     * 交换两个停留点的位置
     * @param index1 第一个停留点索引
     * @param index2 第二个停留点索引
     */
    public void swapStops(int index1, int index2) {
        if (index1 < 0 || index1 >= stops.size() || index2 < 0 || index2 >= stops.size()) {
            throw new IndexOutOfBoundsException("索引越界");
        }
        
        Collections.swap(stops, index1, index2);
        updateRouteStats();
    }

    /**
     * 更新路线统计信息
     */
    private void updateRouteStats() {
        // 重新计算总时间
        Duration totalDuration = Duration.ZERO;
        for (RouteStop stop : stops) {
            totalDuration = totalDuration.plus(stop.getStayDuration());
        }
        
        // 计算行走时间（假设每个景点之间平均需要10分钟）
        // 实际应用中可以根据景点间距离和步行速度更精确计算
        if (stops.size() > 1) {
            totalDuration = totalDuration.plus(Duration.ofMinutes(10 * (stops.size() - 1)));
        }
        
        this.estimatedDuration = totalDuration;
        
        // 计算总距离
        this.totalDistance = 0.0;
        for (int i = 0; i < stops.size() - 1; i++) {
            ScenicSpot current = stops.get(i).getSpot();
            ScenicSpot next = stops.get(i + 1).getSpot();
            this.totalDistance += current.distanceTo(next);
        }
        
        // 检查无障碍友好性
        this.isAccessible = true;
        for (RouteStop stop : stops) {
            if (!stop.getSpot().isAccessible()) {
                this.isAccessible = false;
                break;
            }
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNameZh() {
        return nameZh;
    }

    public void setNameZh(String nameZh) {
        this.nameZh = nameZh;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getDescriptionZh() {
        return descriptionZh;
    }

    public void setDescriptionZh(String descriptionZh) {
        this.descriptionZh = descriptionZh;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public List<RouteStop> getStops() {
        return Collections.unmodifiableList(stops);
    }

    public Duration getEstimatedDuration() {
        return estimatedDuration;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    public RouteType getType() {
        return type;
    }

    public void setType(RouteType type) {
        this.type = type;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    /**
     * 根据当前语言获取路线名称
     * @param isEnglish 是否使用英文
     * @return 路线名称
     */
    public String getName(boolean isEnglish) {
        return isEnglish ? nameEn : nameZh;
    }

    /**
     * 根据当前语言获取路线描述
     * @param isEnglish 是否使用英文
     * @return 路线描述
     */
    public String getDescription(boolean isEnglish) {
        return isEnglish ? descriptionEn : descriptionZh;
    }

    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", nameZh='" + nameZh + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", stops=" + stops.size() +
                ", duration=" + estimatedDuration.toMinutes() + " min" +
                ", distance=" + String.format("%.2f", totalDistance) + " m" +
                '}';
    }
}