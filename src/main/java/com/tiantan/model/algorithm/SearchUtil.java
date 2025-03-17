package com.tiantan.model.algorithm;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 查找工具类
 */
public class SearchUtil {
    
    /**
     * 二分查找算法
     * 
     * @param <T> 数据类型
     * @param array 已排序数组
     * @param low 起始索引
     * @param high 结束索引
     * @param comparator 比较函数，返回负数表示小于，0表示等于，正数表示大于
     * @return 查找到的元素索引，若未找到则返回-1
     */
    public static <T> int binarySearch(T[] array, int low, int high, 
                                     Function<T, Integer> comparator) {
        while (low <= high) {
            int mid = low + (high - low) / 2;
            
            int comp = comparator.apply(array[mid]);
            
            if (comp == 0) {
                return mid; // 找到匹配项
            } else if (comp < 0) {
                low = mid + 1; // 在右半部分查找
            } else {
                high = mid - 1; // 在左半部分查找
            }
        }
        
        return -1; // 未找到
    }
    
    /**
     * 顺序查找算法
     * 
     * @param <T> 数据类型
     * @param array 数组
     * @param size 数组中元素的个数
     * @param predicate 判断是否匹配的函数
     * @return 查找到的元素索引，若未找到则返回-1
     */
    public static <T> int sequentialSearch(T[] array, int size, Function<T, Boolean> predicate) {
        for (int i = 0; i < size; i++) {
            if (predicate.apply(array[i])) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * KMP字符串匹配算法 - 用于关键词搜索
     * 
     * @param text 文本字符串
     * @param pattern 匹配模式
     * @return 第一个匹配位置，若未找到则返回-1
     */
    public static int kmpSearch(String text, String pattern) {
        int n = text.length();
        int m = pattern.length();
        
        if (m == 0) {
            return 0;
        }
        
        // 构建next数组
        int[] next = new int[m];
        computeNext(pattern, next);
        
        int i = 0, j = 0;
        while (i < n) {
            if (j == -1 || text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    return i - m; // 找到匹配
                }
            } else {
                j = next[j];
            }
        }
        
        return -1; // 未找到匹配
    }
    
    /**
     * 计算KMP算法的next数组
     */
    private static void computeNext(String pattern, int[] next) {
        int m = pattern.length();
        next[0] = -1;
        
        int i = 0, j = -1;
        while (i < m - 1) {
            if (j == -1 || pattern.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                next[i] = j;
            } else {
                j = next[j];
            }
        }
    }
    
    /**
     * 模糊查找 - 基于编辑距离，用于搜索建议
     * 
     * @param <T> 数据类型
     * @param array 数组
     * @param size 数组大小
     * @param query 查询词
     * @param stringExtractor 从数组元素中提取字符串的函数
     * @param maxDistance 最大允许的编辑距离
     * @return 最匹配的元素索引，若未找到合适的匹配则返回-1
     */
    public static <T> int fuzzySearch(T[] array, int size, String query, 
                                   Function<T, String> stringExtractor, int maxDistance) {
        int bestIndex = -1;
        int bestDistance = Integer.MAX_VALUE;
        
        for (int i = 0; i < size; i++) {
            String str = stringExtractor.apply(array[i]);
            int distance = levenshteinDistance(query.toLowerCase(), str.toLowerCase());
            
            if (distance <= maxDistance && distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
            }
        }
        
        return bestIndex;
    }
    
    /**
     * 计算Levenshtein编辑距离
     */
    public static int levenshteinDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        
        return dp[m][n];
    }
}