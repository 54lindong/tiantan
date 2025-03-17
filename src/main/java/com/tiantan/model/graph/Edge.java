package com.tiantan.model.graph;

/**
 * 边类
 */
public class Edge {
    private Vertex from;         // 起点
    private Vertex to;           // 终点
    private double weight;       // 权重（距离/时间等）
    private EdgeType type;       // 路径类型
    private boolean isCrowded;   // 是否拥挤

    /**
     * 构造函数
     * @param from 起点
     * @param to 终点
     * @param weight 权重
     * @param type 路径类型
     */
    public Edge(Vertex from, Vertex to, double weight, EdgeType type) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.type = type;
        this.isCrowded = false;
    }

    /**
     * 获取起点
     * @return 起点顶点
     */
    public Vertex getFrom() {
        return from;
    }

    /**
     * 获取终点
     * @return 终点顶点
     */
    public Vertex getTo() {
        return to;
    }

    /**
     * 获取权重
     * @return 权重值
     */
    public double getWeight() {
        return weight;
    }

    /**
     * 设置权重
     * @param weight 权重值
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * 获取路径类型
     * @return 路径类型
     */
    public EdgeType getType() {
        return type;
    }

    /**
     * 设置路径类型
     * @param type 路径类型
     */
    public void setType(EdgeType type) {
        this.type = type;
    }

    /**
     * 是否拥挤
     * @return 如果拥挤返回true
     */
    public boolean isCrowded() {
        return isCrowded;
    }

    /**
     * 设置拥挤状态
     * @param crowded 拥挤状态
     */
    public void setCrowded(boolean crowded) {
        this.isCrowded = crowded;
    }

    /**
     * 获取考虑拥挤因素的实际权重
     * 拥挤时权重增加50%
     * @return 实际权重
     */
    public double getEffectiveWeight() {
        return isCrowded ? weight * 1.5 : weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return from.equals(edge.from) && to.equals(edge.to);
    }

    @Override
    public int hashCode() {
        return 31 * from.hashCode() + to.hashCode();
    }
}