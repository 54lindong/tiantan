package com.tiantan.model.algorithm;

import com.tiantan.model.data.ScenicSpot;
import com.tiantan.model.graph.Edge;
import com.tiantan.model.graph.ScenicGraph;
import com.tiantan.model.graph.Vertex;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 最短路径算法实现类
 */
public class ShortestPath {

    /**
     * Dijkstra算法求解单源最短路径
     * 
     * @param graph 景区图
     * @param sourceId 起点景点ID
     * @param weightFunction 权重计算函数
     * @return 最短路径树，键为顶点ID，值为前驱顶点ID
     */
    public static Map<Integer, Integer> dijkstra(ScenicGraph graph, int sourceId, 
                                              Function<Edge, Double> weightFunction) {
        // 获取起点顶点
        Vertex source = graph.getVertex(sourceId);
        if (source == null) {
            return Collections.emptyMap();
        }
        
        // 初始化距离和前驱顶点表
        Map<Integer, Double> distance = new HashMap<>();
        Map<Integer, Integer> predecessor = new HashMap<>();
        Set<Integer> settled = new HashSet<>();
        
        // 优先队列，按距离排序
        PriorityQueue<VertexDistance> queue = new PriorityQueue<>();
        
        // 初始化所有顶点的距离为无穷大
        for (Vertex vertex : graph.getVertices()) {
            int id = vertex.getSpot().getId();
            distance.put(id, Double.POSITIVE_INFINITY);
            predecessor.put(id, null);
        }
        
        // 起点距离为0
        distance.put(sourceId, 0.0);
        queue.offer(new VertexDistance(sourceId, 0.0));
        
        // 主循环
        while (!queue.isEmpty()) {
            VertexDistance current = queue.poll();
            int currentId = current.vertexId;
            
            // 如果已处理过，则跳过
            if (settled.contains(currentId)) {
                continue;
            }
            
            // 标记为已处理
            settled.add(currentId);
            
            // 处理所有邻接边
            Vertex currentVertex = graph.getVertex(currentId);
            for (Edge edge : currentVertex.getAdjacent()) {
                Vertex neighbor = edge.getTo();
                int neighborId = neighbor.getSpot().getId();
                
                // 如果已处理过，则跳过
                if (settled.contains(neighborId)) {
                    continue;
                }
                
                // 计算通过当前顶点到邻接顶点的距离
                double edgeWeight = weightFunction.apply(edge);
                double newDistance = distance.get(currentId) + edgeWeight;
                
                // 如果找到更短的路径，则更新
                if (newDistance < distance.get(neighborId)) {
                    distance.put(neighborId, newDistance);
                    predecessor.put(neighborId, currentId);
                    queue.offer(new VertexDistance(neighborId, newDistance));
                }
            }
        }
        
        return predecessor;
    }
    
    /**
     * 根据前驱顶点表构建从起点到终点的路径
     * 
     * @param graph 景区图
     * @param predecessor 前驱顶点表
     * @param targetId 终点景点ID
     * @return 路径上的景点列表，从起点到终点
     */
    public static List<ScenicSpot> constructPath(ScenicGraph graph, Map<Integer, Integer> predecessor, int targetId) {
        List<ScenicSpot> path = new ArrayList<>();
        
        // 检查终点是否可达
        if (!predecessor.containsKey(targetId) || predecessor.get(targetId) == null && targetId != getSourceId(predecessor)) {
            return path;
        }
        
        // 从终点回溯到起点
        for (Integer at = targetId; at != null; at = predecessor.get(at)) {
            Vertex vertex = graph.getVertex(at);
            path.add(0, vertex.getSpot()); // 在路径开头插入景点
        }
        
        return path;
    }
    
    /**
     * 获取前驱顶点表中的起点ID
     */
    private static Integer getSourceId(Map<Integer, Integer> predecessor) {
        for (Map.Entry<Integer, Integer> entry : predecessor.entrySet()) {
            if (entry.getValue() == null) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * 计算路径的总长度
     * 
     * @param graph 景区图
     * @param path 路径上的景点列表
     * @param weightFunction 权重计算函数
     * @return 路径总长度
     */
    public static double calculatePathLength(ScenicGraph graph, List<ScenicSpot> path, 
                                          Function<Edge, Double> weightFunction) {
        if (path.size() < 2) {
            return 0.0;
        }
        
        double totalLength = 0.0;
        
        for (int i = 0; i < path.size() - 1; i++) {
            int fromId = path.get(i).getId();
            int toId = path.get(i + 1).getId();
            
            Vertex fromVertex = graph.getVertex(fromId);
            Edge edge = fromVertex.getEdgeTo(toId);
            
            if (edge != null) {
                totalLength += weightFunction.apply(edge);
            }
        }
        
        return totalLength;
    }
    
    /**
     * A*算法求解单点对最短路径，适用于有启发式信息的情况
     * 
     * @param graph 景区图
     * @param sourceId 起点景点ID
     * @param targetId 终点景点ID
     * @param weightFunction 边权重计算函数
     * @param heuristicFunction 启发式函数
     * @return 最短路径上的景点列表，从起点到终点
     */
    public static List<ScenicSpot> aStar(ScenicGraph graph, int sourceId, int targetId,
                                      Function<Edge, Double> weightFunction,
                                      BiFunction<Vertex, Vertex, Double> heuristicFunction) {
        // 获取起点和终点顶点
        Vertex source = graph.getVertex(sourceId);
        Vertex target = graph.getVertex(targetId);
        
        if (source == null || target == null) {
            return Collections.emptyList();
        }
        
        // 已探索的顶点集合
        Set<Integer> closedSet = new HashSet<>();
        
        // 待探索的顶点集合
        Set<Integer> openSet = new HashSet<>();
        openSet.add(sourceId);
        
        // 从起点到当前点的实际代价
        Map<Integer, Double> gScore = new HashMap<>();
        for (Vertex vertex : graph.getVertices()) {
            gScore.put(vertex.getSpot().getId(), Double.POSITIVE_INFINITY);
        }
        gScore.put(sourceId, 0.0);
        
        // 从起点经由当前点到终点的估计总代价
        Map<Integer, Double> fScore = new HashMap<>();
        for (Vertex vertex : graph.getVertices()) {
            fScore.put(vertex.getSpot().getId(), Double.POSITIVE_INFINITY);
        }
        fScore.put(sourceId, heuristicFunction.apply(source, target));
        
        // 记录路径的前驱顶点
        Map<Integer, Integer> cameFrom = new HashMap<>();
        
        // 主循环
        while (!openSet.isEmpty()) {
            // 获取fScore最小的顶点
            int current = getLowestFScore(openSet, fScore);
            
            // 如果到达终点，构建并返回路径
            if (current == targetId) {
                return reconstructPath(graph, cameFrom, current);
            }
            
            // 将当前顶点从待探索集合移到已探索集合
            openSet.remove(current);
            closedSet.add(current);
            
            // 处理所有邻接顶点
            Vertex currentVertex = graph.getVertex(current);
            for (Edge edge : currentVertex.getAdjacent()) {
                Vertex neighbor = edge.getTo();
                int neighborId = neighbor.getSpot().getId();
                
                // 如果邻接顶点已经探索过，则跳过
                if (closedSet.contains(neighborId)) {
                    continue;
                }
                
                // 计算经由当前顶点到达邻接顶点的代价
                double tentativeGScore = gScore.get(current) + weightFunction.apply(edge);
                
                // 如果邻接顶点不在待探索集合中，添加它
                if (!openSet.contains(neighborId)) {
                    openSet.add(neighborId);
                }
                // 如果找到更好的路径，则更新
                else if (tentativeGScore >= gScore.get(neighborId)) {
                    continue;
                }
                
                // 更新路径信息
                cameFrom.put(neighborId, current);
                gScore.put(neighborId, tentativeGScore);
                fScore.put(neighborId, gScore.get(neighborId) + heuristicFunction.apply(neighbor, target));
            }
        }
        
        // 如果无法到达终点，返回空列表
        return Collections.emptyList();
    }
    
    /**
     * 获取fScore最小的顶点ID
     */
    private static int getLowestFScore(Set<Integer> openSet, Map<Integer, Double> fScore) {
        double lowestScore = Double.POSITIVE_INFINITY;
        int lowestId = -1;
        
        for (int id : openSet) {
            double score = fScore.get(id);
            if (score < lowestScore) {
                lowestScore = score;
                lowestId = id;
            }
        }
        
        return lowestId;
    }
    
    /**
     * 根据前驱顶点表重建路径
     */
    private static List<ScenicSpot> reconstructPath(ScenicGraph graph, Map<Integer, Integer> cameFrom, int current) {
        List<ScenicSpot> path = new ArrayList<>();
        
        // 将终点添加到路径中
        path.add(graph.getVertex(current).getSpot());
        
        // 从终点回溯到起点
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, graph.getVertex(current).getSpot());
        }
        
        return path;
    }
    
    /**
     * 用于Dijkstra算法的顶点-距离对
     */
    private static class VertexDistance implements Comparable<VertexDistance> {
        private final int vertexId;
        private final double distance;
        
        VertexDistance(int vertexId, double distance) {
            this.vertexId = vertexId;
            this.distance = distance;
        }
        
        @Override
        public int compareTo(VertexDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }
    
    /**
     * 多标准最短路径算法，考虑距离、时间、拥挤度等多个因素
     * 
     * @param graph 景区图
     * @param sourceId 起点景点ID
     * @param targetId 终点景点ID
     * @param weightFunctions 多个权重计算函数
     * @param weights 各个标准的权重（重要性）
     * @return 最优路径上的景点列表
     */
    public static List<ScenicSpot> multiCriteriaShortestPath(ScenicGraph graph, int sourceId, int targetId,
                                                         List<Function<Edge, Double>> weightFunctions,
                                                         List<Double> weights) {
        if (weightFunctions.size() != weights.size()) {
            throw new IllegalArgumentException("权重函数和权重数量必须相同");
        }
        
        // 定义综合权重计算函数
        Function<Edge, Double> combinedWeightFunction = edge -> {
            double sum = 0.0;
            for (int i = 0; i < weightFunctions.size(); i++) {
                sum += weightFunctions.get(i).apply(edge) * weights.get(i);
            }
            return sum;
        };
        
        // 使用Dijkstra算法求解
        Map<Integer, Integer> predecessor = dijkstra(graph, sourceId, combinedWeightFunction);
        return constructPath(graph, predecessor, targetId);
    }
}