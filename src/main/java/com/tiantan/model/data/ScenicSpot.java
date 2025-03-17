package com.tiantan.model.data;

import java.util.Objects;

/**
 * 景点信息类
 */
public class ScenicSpot implements Comparable<ScenicSpot> {
    private int id;                  // 景点ID
    private String nameZh;           // 中文名称
    private String nameEn;           // 英文名称
    private String descriptionZh;    // 中文描述
    private String descriptionEn;    // 英文描述
    private double x;                // X坐标
    private double y;                // Y坐标
    private String category;         // 景点类别
    private int visitTime;           // 建议游览时间(分钟)
    private String imageUrl;         // 图片路径
    private int popularity;          // 热门程度(1-100)
    private boolean isAccessible;    // 无障碍设施
    private double entranceFee;      // 门票价格

    // 构造函数
    public ScenicSpot(int id, String nameZh, String nameEn, String descriptionZh, String descriptionEn,
                     double x, double y, String category, int visitTime, String imageUrl,
                     int popularity, boolean isAccessible, double entranceFee) {
        this.id = id;
        this.nameZh = nameZh;
        this.nameEn = nameEn;
        this.descriptionZh = descriptionZh;
        this.descriptionEn = descriptionEn;
        this.x = x;
        this.y = y;
        this.category = category;
        this.visitTime = visitTime;
        this.imageUrl = imageUrl;
        this.popularity = popularity;
        this.isAccessible = isAccessible;
        this.entranceFee = entranceFee;
    }

    // Getters 和 Setters
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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(int visitTime) {
        this.visitTime = visitTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    public void setAccessible(boolean accessible) {
        isAccessible = accessible;
    }

    public double getEntranceFee() {
        return entranceFee;
    }

    public void setEntranceFee(double entranceFee) {
        this.entranceFee = entranceFee;
    }

    // 根据当前语言返回名称
    public String getName(boolean isEnglish) {
        return isEnglish ? nameEn : nameZh;
    }

    // 根据当前语言返回描述
    public String getDescription(boolean isEnglish) {
        return isEnglish ? descriptionEn : descriptionZh;
    }

    // 计算与另一个景点的距离
    public double distanceTo(ScenicSpot other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    // 实现Comparable接口，默认按热门程度排序
    @Override
    public int compareTo(ScenicSpot other) {
        return Integer.compare(other.popularity, this.popularity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScenicSpot that = (ScenicSpot) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ScenicSpot{" +
                "id=" + id +
                ", nameZh='" + nameZh + '\'' +
                ", nameEn='" + nameEn + '\'' +
                '}';
    }
}