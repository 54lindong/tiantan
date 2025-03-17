package com.tiantan.model.graph;

import com.tiantan.model.data.ScenicSpot;

import java.util.ArrayList;
import java.util.List;

/**
 * 顶点类
 */
public class Vertex {
    private ScenicSpot spot;             // 关联的景点
    private List<Edge> adjacent;         // 邻接边列表

    /**
     * 构造函数
     * @param spot 景点
     */
    public Vertex(ScenicSpot spot) {
        this.spot = spot;
        this.adjacent = new ArrayList<>();
    }

    /**
     * 获取景点
     * @return 关联的景点
     */
    public ScenicSpot getSpot() {
        return spot;
    }

    /**
     * 获取所有邻接边
     * @return 邻接边列表
     */
    public List<Edge> getAdjacent() {
        return adjacent;
    }

    /**
     * 添加邻接边
     * @param edge 边
     */
    public void addAdjacent(Edge edge) {
        adjacent.add(edge);
    }

    /**
     * 根据目标顶点ID删除邻接边
     * @param toId 目标顶点ID
     * @return 如果删除成功返回true
     */
    public boolean removeAdjacentTo(int toId) {
        return adjacent.removeIf(edge -> edge.getTo().getSpot().getId() == toId);
    }

    /**
     * 根据顶点ID移除相关邻接边
     * @param spotId 顶点ID
     */
    public void removeAdjacent(int spotId) {
        adjacent.removeIf(edge -> 
            edge.getFrom().getSpot().getId() == spotId || 
            edge.getTo().getSpot().getId() == spotId
        );
    }

    /**
     * 获取到指定顶点的边
     * @param toId 目标顶点ID
     * @return 匹配的边，如果不存在返回null
     */
    public Edge getEdgeTo(int toId) {
        for (Edge edge : adjacent) {
            if (edge.getTo().getSpot().getId() == toId) {
                return edge;
            }
        }
        return null;
    }

    /**
     * 获取邻接顶点数量
     * @return 邻接顶点数量
     */
    public int getDegree() {
        return adjacent.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return spot.equals(vertex.spot);
    }

    @Override
    public int hashCode() {
        return spot.hashCode();
    }
}