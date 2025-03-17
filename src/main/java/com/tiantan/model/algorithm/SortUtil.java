package com.tiantan.model.algorithm;

import java.util.Comparator;

/**
 * 排序工具类
 */
public class SortUtil {
    
    /**
     * 快速排序算法
     * 
     * @param <T> 数据类型
     * @param array 待排序数组
     * @param low 起始索引
     * @param high 结束索引
     * @param comparator 比较器
     */
    public static <T> void quickSort(T[] array, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            int pivotIndex = partition(array, low, high, comparator);
            quickSort(array, low, pivotIndex - 1, comparator);
            quickSort(array, pivotIndex + 1, high, comparator);
        }
    }
    
    /**
     * 快速排序的分区函数
     */
    private static <T> int partition(T[] array, int low, int high, Comparator<T> comparator) {
        T pivot = array[high];
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (comparator.compare(array[j], pivot) <= 0) {
                i++;
                swap(array, i, j);
            }
        }
        
        swap(array, i + 1, high);
        return i + 1;
    }
    
    /**
     * 归并排序算法
     * 
     * @param <T> 数据类型
     * @param array 待排序数组
     * @param temp 临时数组
     * @param low 起始索引
     * @param high 结束索引
     * @param comparator 比较器
     */
    public static <T> void mergeSort(T[] array, T[] temp, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            int mid = low + (high - low) / 2;
            mergeSort(array, temp, low, mid, comparator);
            mergeSort(array, temp, mid + 1, high, comparator);
            merge(array, temp, low, mid, high, comparator);
        }
    }
    
    /**
     * 归并排序的合并函数
     */
    private static <T> void merge(T[] array, T[] temp, int low, int mid, int high, Comparator<T> comparator) {
        // 复制到临时数组
        for (int i = low; i <= high; i++) {
            temp[i] = array[i];
        }
        
        int i = low;     // 左半部分起始索引
        int j = mid + 1; // 右半部分起始索引
        int k = low;     // 合并后数组的当前索引
        
        // 合并两个有序数组
        while (i <= mid && j <= high) {
            if (comparator.compare(temp[i], temp[j]) <= 0) {
                array[k++] = temp[i++];
            } else {
                array[k++] = temp[j++];
            }
        }
        
        // 复制剩余元素
        while (i <= mid) {
            array[k++] = temp[i++];
        }
        
        // 注意：右半部分的剩余元素已经在正确位置，不需要复制
    }
    
    /**
     * 堆排序算法
     * 
     * @param <T> 数据类型
     * @param array 待排序数组
     * @param size 数组大小
     * @param comparator 比较器
     */
    public static <T> void heapSort(T[] array, int size, Comparator<T> comparator) {
        // 构建最大堆
        for (int i = size / 2 - 1; i >= 0; i--) {
            heapify(array, size, i, comparator);
        }
        
        // 一个个从堆中提取元素
        for (int i = size - 1; i > 0; i--) {
            swap(array, 0, i);
            heapify(array, i, 0, comparator);
        }
    }
    
    /**
     * 堆排序的堆化函数
     */
    private static <T> void heapify(T[] array, int size, int rootIndex, Comparator<T> comparator) {
        int largest = rootIndex;
        int left = 2 * rootIndex + 1;
        int right = 2 * rootIndex + 2;
        
        if (left < size && comparator.compare(array[left], array[largest]) > 0) {
            largest = left;
        }
        
        if (right < size && comparator.compare(array[right], array[largest]) > 0) {
            largest = right;
        }
        
        if (largest != rootIndex) {
            swap(array, rootIndex, largest);
            heapify(array, size, largest, comparator);
        }
    }
    
    /**
     * 交换数组中的两个元素
     */
    private static <T> void swap(T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    
    /**
     * 插入排序算法 - 适用于小数据集或部分有序数据
     * 
     * @param <T> 数据类型
     * @param array 待排序数组
     * @param size 数组大小
     * @param comparator 比较器
     */
    public static <T> void insertionSort(T[] array, int size, Comparator<T> comparator) {
        for (int i = 1; i < size; i++) {
            T key = array[i];
            int j = i - 1;
            
            while (j >= 0 && comparator.compare(array[j], key) > 0) {
                array[j + 1] = array[j];
                j--;
            }
            
            array[j + 1] = key;
        }
    }
}