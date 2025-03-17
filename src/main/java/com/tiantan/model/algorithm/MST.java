package com.tiantan.model.algorithm;

import com.tiantan.model.data.ScenicSpot;
import com.tiantan.model.graph.Edge;
import com.tiantan.model.graph.ScenicGraph;
import com.tiantan.model.graph.Vertex;

import java.util.*;
import java.util.function.Function;

/**
 * 最小生成树算法实现类
 */
public class MST {
    
    /**
     * Kruskal算法实现最小生成树
     * 
     * @param graph 景区图
     * @param weightFunction 权重计算函数
     * @return 最小生成树的边集合
     */
    public static List<Edge> kruskal(ScenicGraph graph, Function<Edge, Double> weightFunction) {
        List<Edge> mst = new ArrayList<>();
        
        // 获取所有边并按权重排序
        List<Edge> allEdges = new ArrayList<>(graph.getEdges());
        allEdges.sort(Comparator.comparing(weightFunction));
        
        // 初始化并查集
        Map<Integer, Integer> parent = new HashMap<>();
        for (Vertex vertex : graph.getVertices()) {
            int id = vertex.getSpot().getId();
            parent.put(id, id); // 每个顶点初始是自己的代表元素
        }
        
        // 处理每条边
        for (Edge edge : allEdges) {
            int fromId = edge.getFrom().getSpot().getId();
            int toId = edge.getTo().getSpot().getId();
            
            // 如果两个顶点不在同一个集合中，则添加这条边
            if (find(parent, fromId) != find(parent, toId)) {
                mst.add(edge);
                union(parent, fromId, toId);
            }
            
            // 如果已经有n-1条边，MST构建完成
            if (mst.size() == graph.getVertexCount() - 1) {
                break;
            }
        }
        
        return mst;
    }
    
    /**
     * 并查集查找操作（带路径压缩）
     */
    private static int find(Map<Integer, Integer> parent, int x) {
        if (parent.get(x) != x) {
            parent.put(x, find(parent, parent.get(x)));
        }
        return parent.get(x);
    }
    
    /**
     * 并查集合并操作
     */
    private static void union(Map<Integer, Integer> parent, int x, int y) {
        int rootX = find(parent, x);
        int rootY = find(parent, y);
        parent.put(rootX, rootY);
    }
    
    /**
     * Prim算法实现最小生成树
     * 
     * @param graph 景区图
     * @param startId 起始顶点ID
     * @param weightFunction 权重计算函数
     * @return 最小生成树的边集合
     */
    public static List<Edge> prim(ScenicGraph graph, int startId, Function<Edge, Double> weightFunction) {
        List<Edge> mst = new ArrayList<>();
        
        // 获取起始顶点
        Vertex start = graph.getVertex(startId);
        if (start == null || graph.getVertexCount() == 0) {
            return mst;
        }
        
        // 已包含在MST中的顶点
        Set<Integer> included = new HashSet<>();
        included.add(startId);
        
        // 候选边优先队列，按权重排序
        PriorityQueue<EdgeWithWeight> candidateEdges = new PriorityQueue<>();
        
        // 将起始顶点的所有邻接边加入候选集
        for (Edge edge : start.getAdjacent()) {
            candidateEdges.add(new EdgeWithWeight(edge, weightFunction.apply(edge)));
        }
        
        // 主循环：直到所有顶点都包含在MST中，或者没有更多的候选边
        while (!candidateEdges.isEmpty() && included.size() < graph.getVertexCount()) {
            // 取出权重最小的边
            Edge minEdge = candidateEdges.poll().edge;
            int toId = minEdge.getTo().getSpot().getId();
            
            // 如果目标顶点已经在MST中，则跳过
            if (included.contains(toId)) {
                continue;
            }
            
            // 将边添加到MST中
            mst.add(minEdge);
            included.add(toId);
            
            // 将新顶点的所有邻接边加入候选集
            Vertex newVertex = graph.getVertex(toId);
            for (Edge edge : newVertex.getAdjacent()) {
                int neighborId = edge.getTo().getSpot().getId();
                // 只考虑不在MST中的顶点
                if (!included.contains(neighborId)) {
                    candidateEdges.add(new EdgeWithWeight(edge, weightFunction.apply(edge)));
                }
            }
        }
        
        return mst;
    }
    
    /**
     * 辅助类：带权重的边
     */
    private static class EdgeWithWeight implements Comparable<EdgeWithWeight> {
        private final Edge edge;
        private final double weight;
        
        EdgeWithWeight(Edge edge, double weight) {
            this.edge = edge;
            this.weight = weight;
        }
        
        @Override
        public int compareTo(EdgeWithWeight other) {
            return Double.compare(this.weight, other.weight);
        }
    }
    
    /**
     * 构建MST的路径
     * 
     * @param mstEdges 最小生成树的边集合
     * @param startId 起始顶点ID
     * @return 遍历路径上的景点列表（类似于先序遍历）
     */
    public static List<ScenicSpot> constructMSTPath(List<Edge> mstEdges, int startId) {
        if (mstEdges.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 构建邻接表
        Map<Integer, List<Integer>> adjList = new HashMap<>();
        for (Edge edge : mstEdges) {
            int fromId = edge.getFrom().getSpot().getId();
            int toId = edge.getTo().getSpot().getId();
            
            // 添加正向边
            adjList.computeIfAbsent(fromId, k -> new ArrayList<>()).add(toId);
            
            // 添加反向边（因为MST是无向的）
            adjList.computeIfAbsent(toId, k -> new ArrayList<>()).add(fromId);
        }
        
        // 保存遍历结果
        List<ScenicSpot> path = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        
        // 获取第一条边的任一端点作为起点（如果未指定）
        if (startId == -1) {
            Edge firstEdge = mstEdges.get(0);
            startId = firstEdge.getFrom().getSpot().getId();
        }
        
        // 执行DFS遍历
        dfsTraversal(adjList, startId, visited, path, mstEdges);
        
        return path;
    }
    
    /**
     * DFS遍历MST
     */
    private static void dfsTraversal(Map<Integer, List<Integer>> adjList, int current, 
                                  Set<Integer> visited, List<ScenicSpot> path, List<Edge> mstEdges) {
        // 标记当前顶点为已访问
        visited.add(current);
        
        // 将当前顶点的景点添加到路径
        for (Edge edge : mstEdges) {
            if (edge.getFrom().getSpot().getId() == current) {
                path.add(edge.getFrom().getSpot());
                break;
            } else if (edge.getTo().getSpot().getId() == current) {
                path.add(edge.getTo().getSpot());
                break;
            }
        }
        
        // 访问所有未访问的邻接顶点
        List<Integer> neighbors = adjList.getOrDefault(current, Collections.emptyList());
        for (int neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                dfsTraversal(adjList, neighbor, visited, path, mstEdges);
            }
        }
    }
    
    /**
     * 计算最优游览路线（近似TSP问题解）
     * 使用最小生成树两倍近似算法
     * 
     * @param graph 景区图
     * @param startId 起始顶点ID
     * @param weightFunction 权重计算函数
     * @return 游览路线上的景点列表
     */
    public static List<ScenicSpot> approximateTSP(ScenicGraph graph, int startId, 
                                              Function<Edge, Double> weightFunction) {
        // 获取MST
        List<Edge> mstEdges = prim(graph, startId, weightFunction);
        
        // 构建MST的先序遍历
        List<ScenicSpot> preorderWalk = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        
        // 获取起始顶点
        Vertex start = graph.getVertex(startId);
        if (start == null) {
            return Collections.emptyList();
        }
        
        // 构建邻接表
        Map<Integer, List<Vertex>> adjList = new HashMap<>();
        for (Edge edge : mstEdges) {
            int fromId = edge.getFrom().getSpot().getId();
            int toId = edge.getTo().getSpot().getId();
            
            // 添加正向边
            adjList.computeIfAbsent(fromId, k -> new ArrayList<>()).add(edge.getTo());
            
            // 添加反向边
            adjList.computeIfAbsent(toId, k -> new ArrayList<>()).add(edge.getFrom());
        }
        
        // 执行先序遍历
        preorderTraversal(adjList, start, visited, preorderWalk);
        
        // 添加起点以形成环路（如果需要返回起点）
        if (!preorderWalk.isEmpty() && preorderWalk.get(0).getId() != startId) {
            preorderWalk.add(start.getSpot());
        }
        
        return preorderWalk;
    }
    
    /**
     * MST的先序遍历
     */
    private static void preorderTraversal(Map<Integer, List<Vertex>> adjList, Vertex current, 
                                       Set<Integer> visited, List<ScenicSpot> preorderWalk) {
        int currentId = current.getSpot().getId();
        visited.add(currentId);
        preorderWalk.add(current.getSpot());
        
        // 访问所有未访问的邻接顶点
        List<Vertex> neighbors = adjList.getOrDefault(currentId, Collections.emptyList());
        for (Vertex neighbor : neighbors) {
            int neighborId = neighbor.getSpot().getId();
            if (!visited.contains(neighborId)) {
                preorderTraversal(adjList, neighbor, visited, preorderWalk);
            }
        }
    }
}