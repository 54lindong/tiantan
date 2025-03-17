package com.tiantan.model.graph;

import com.tiantan.model.data.ScenicSpot;

import java.util.*;

/**
 * 景区图类 - 用于表示景点之间的连接关系
 * 采用邻接表表示法实现
 */
public class ScenicGraph {
    private Map<Integer, Vertex> vertices;  // 顶点集合，键为景点ID
    private List<Edge> edges;               // 边集合
    private boolean isDirected;             // 是否为有向图

    /**
     * 构造函数
     * @param isDirected 是否为有向图
     */
    public ScenicGraph(boolean isDirected) {
        this.vertices = new HashMap<>();
        this.edges = new ArrayList<>();
        this.isDirected = isDirected;
    }

    /**
     * 添加顶点
     * @param spot 景点
     * @return 如果添加成功返回true，如果已存在返回false
     */
    public boolean addVertex(ScenicSpot spot) {
        if (vertices.containsKey(spot.getId())) {
            return false;
        }
        vertices.put(spot.getId(), new Vertex(spot));
        return true;
    }

    /**
     * 添加边
     * @param from 起点ID
     * @param to 终点ID
     * @param weight 权重（距离/时间等）
     * @param type 路径类型（步行、车行等）
     * @return 如果添加成功返回true，如果顶点不存在返回false
     */
    public boolean addEdge(int from, int to, double weight, EdgeType type) {
        Vertex fromVertex = vertices.get(from);
        Vertex toVertex = vertices.get(to);
        
        if (fromVertex == null || toVertex == null) {
            return false;
        }
        
        // 创建新边
        Edge edge = new Edge(fromVertex, toVertex, weight, type);
        edges.add(edge);
        
        // 添加到邻接表
        fromVertex.addAdjacent(edge);
        
        // 如果是无向图，则添加反向边
        if (!isDirected) {
            Edge reverseEdge = new Edge(toVertex, fromVertex, weight, type);
            edges.add(reverseEdge);
            toVertex.addAdjacent(reverseEdge);
        }
        
        return true;
    }

    /**
     * 删除顶点
     * @param spotId 顶点ID
     * @return 如果删除成功返回true
     */
    public boolean removeVertex(int spotId) {
        Vertex vertex = vertices.remove(spotId);
        if (vertex == null) {
            return false;
        }
        
        // 删除所有与该顶点相关的边
        edges.removeIf(edge -> 
            edge.getFrom().getSpot().getId() == spotId || 
            edge.getTo().getSpot().getId() == spotId
        );
        
        // 从其他顶点的邻接表中删除
        for (Vertex v : vertices.values()) {
            v.removeAdjacent(spotId);
        }
        
        return true;
    }

    /**
     * 删除边
     * @param from 起点ID
     * @param to 终点ID
     * @return 如果删除成功返回true
     */
    public boolean removeEdge(int from, int to) {
        Vertex fromVertex = vertices.get(from);
        Vertex toVertex = vertices.get(to);
        
        if (fromVertex == null || toVertex == null) {
            return false;
        }
        
        // 删除边集合中的边
        boolean removed = edges.removeIf(edge -> 
            edge.getFrom().getSpot().getId() == from && 
            edge.getTo().getSpot().getId() == to
        );
        
        // 从邻接表中删除
        fromVertex.removeAdjacentTo(to);
        
        // 如果是无向图，也删除反向边
        if (!isDirected) {
            edges.removeIf(edge -> 
                edge.getFrom().getSpot().getId() == to && 
                edge.getTo().getSpot().getId() == from
            );
            toVertex.removeAdjacentTo(from);
        }
        
        return removed;
    }

    /**
     * 获取顶点
     * @param spotId 景点ID
     * @return 对应的顶点对象
     */
    public Vertex getVertex(int spotId) {
        return vertices.get(spotId);
    }

    /**
     * 获取所有顶点
     * @return 顶点集合
     */
    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    /**
     * 获取所有边
     * @return 边集合
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * 获取顶点数量
     * @return 顶点数量
     */
    public int getVertexCount() {
        return vertices.size();
    }

    /**
     * 获取边数量
     * @return 边数量
     */
    public int getEdgeCount() {
        return edges.size();
    }

    /**
     * 深度优先遍历
     * @param startId 起始顶点ID
     * @return 遍历结果列表
     */
    public List<ScenicSpot> dfs(int startId) {
        Vertex start = vertices.get(startId);
        if (start == null) {
            return Collections.emptyList();
        }
        
        List<ScenicSpot> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        
        // 调用递归DFS
        dfsHelper(start, visited, result);
        
        return result;
    }

    /**
     * DFS递归辅助函数
     */
    private void dfsHelper(Vertex vertex, Set<Integer> visited, List<ScenicSpot> result) {
        int spotId = vertex.getSpot().getId();
        visited.add(spotId);
        result.add(vertex.getSpot());
        
        // 访问所有邻接点
        for (Edge edge : vertex.getAdjacent()) {
            Vertex neighbor = edge.getTo();
            if (!visited.contains(neighbor.getSpot().getId())) {
                dfsHelper(neighbor, visited, result);
            }
        }
    }

    /**
     * 广度优先遍历
     * @param startId 起始顶点ID
     * @return 遍历结果列表
     */
    public List<ScenicSpot> bfs(int startId) {
        Vertex start = vertices.get(startId);
        if (start == null) {
            return Collections.emptyList();
        }
        
        List<ScenicSpot> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Queue<Vertex> queue = new LinkedList<>();
        
        // 标记起始顶点为已访问并入队
        visited.add(startId);
        queue.offer(start);
        
        while (!queue.isEmpty()) {
            Vertex current = queue.poll();
            result.add(current.getSpot());
            
            // 访问所有邻接点
            for (Edge edge : current.getAdjacent()) {
                Vertex neighbor = edge.getTo();
                int neighborId = neighbor.getSpot().getId();
                
                if (!visited.contains(neighborId)) {
                    visited.add(neighborId);
                    queue.offer(neighbor);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取两点间所有路径
     * @param fromId 起点ID
     * @param toId 终点ID
     * @return 所有路径列表
     */
    public List<List<ScenicSpot>> getAllPaths(int fromId, int toId) {
        Vertex start = vertices.get(fromId);
        Vertex end = vertices.get(toId);
        
        if (start == null || end == null) {
            return Collections.emptyList();
        }
        
        List<List<ScenicSpot>> allPaths = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        List<ScenicSpot> currentPath = new ArrayList<>();
        
        // 开始搜索路径
        currentPath.add(start.getSpot());
        visited.add(fromId);
        
        findAllPathsDFS(start, end, visited, currentPath, allPaths);
        
        return allPaths;
    }
    
    /**
     * 查找所有路径的DFS辅助函数
     */
    private void findAllPathsDFS(Vertex current, Vertex end, Set<Integer> visited, 
                               List<ScenicSpot> currentPath, List<List<ScenicSpot>> allPaths) {
        // 如果到达终点
        if (current.equals(end)) {
            allPaths.add(new ArrayList<>(currentPath));
            return;
        }
        
        // 访问所有邻接点
        for (Edge edge : current.getAdjacent()) {
            Vertex neighbor = edge.getTo();
            int neighborId = neighbor.getSpot().getId();
            
            if (!visited.contains(neighborId)) {
                // 标记已访问
                visited.add(neighborId);
                currentPath.add(neighbor.getSpot());
                
                // 递归搜索
                findAllPathsDFS(neighbor, end, visited, currentPath, allPaths);
                
                // 回溯
                visited.remove(neighborId);
                currentPath.remove(currentPath.size() - 1);
            }
        }
    }
}