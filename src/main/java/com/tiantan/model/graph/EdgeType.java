package com.tiantan.model.graph;

/**
 * 边类型枚举
 * 定义景区中不同的路径类型
 */
public enum EdgeType {
    WALKING("步行"),
    WHEELCHAIR("轮椅通道"),
    SHUTTLE("景区摆渡车"),
    STAIRS("台阶");
    
    private final String description;
    
    EdgeType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}