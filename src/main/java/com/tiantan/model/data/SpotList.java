package com.tiantan.model.data;

import com.tiantan.model.algorithm.SearchUtil;
import com.tiantan.model.algorithm.SortUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * 景点线性表实现类
 * 采用顺序存储结构实现景点列表的管理
 */
public class SpotList implements Iterable<ScenicSpot> {
    private ScenicSpot[] spots;  // 存储景点的数组
    private int size;           // 当前景点数量
    private static final int DEFAULT_CAPACITY = 16;  // 默认容量

    // 构造函数
    public SpotList() {
        spots = new ScenicSpot[DEFAULT_CAPACITY];
        size = 0;
    }

    public SpotList(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("容量必须为正数");
        }
        spots = new ScenicSpot[initialCapacity];
        size = 0;
    }

    // 添加景点
    public void add(ScenicSpot spot) {
        ensureCapacity(size + 1);
        spots[size++] = spot;
    }

    // 在指定位置插入景点
    public void insert(int index, ScenicSpot spot) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("索引越界: " + index);
        }
        ensureCapacity(size + 1);
        
        // 将index及之后的元素后移一位
        System.arraycopy(spots, index, spots, index + 1, size - index);
        spots[index] = spot;
        size++;
    }

    // 删除景点
    public boolean remove(ScenicSpot spot) {
        for (int i = 0; i < size; i++) {
            if (spots[i].equals(spot)) {
                return removeAt(i);
            }
        }
        return false;
    }

    // 删除指定位置的景点
    public boolean removeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("索引越界: " + index);
        }
        
        // 将index之后的元素前移一位
        System.arraycopy(spots, index + 1, spots, index, size - index - 1);
        spots[--size] = null; // 便于GC回收
        return true;
    }

    // 修改指定位置的景点
    public void set(int index, ScenicSpot spot) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("索引越界: " + index);
        }
        spots[index] = spot;
    }

    // 获取指定位置的景点
    public ScenicSpot get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("索引越界: " + index);
        }
        return spots[index];
    }

    // 获取景点在列表中的位置
    public int indexOf(ScenicSpot spot) {
        for (int i = 0; i < size; i++) {
            if (spots[i].equals(spot)) {
                return i;
            }
        }
        return -1;
    }

    // 列表是否包含指定景点
    public boolean contains(ScenicSpot spot) {
        return indexOf(spot) >= 0;
    }

    // 获取当前景点数量
    public int size() {
        return size;
    }

    // 检查列表是否为空
    public boolean isEmpty() {
        return size == 0;
    }

    // 清空列表
    public void clear() {
        Arrays.fill(spots, 0, size, null);
        size = 0;
    }

    // 确保容量足够
    private void ensureCapacity(int minCapacity) {
        if (minCapacity > spots.length) {
            int newCapacity = spots.length * 2;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            spots = Arrays.copyOf(spots, newCapacity);
        }
    }

    // 转换为数组
    public ScenicSpot[] toArray() {
        return Arrays.copyOf(spots, size);
    }

    // 使用二分查找按ID查找景点
    public ScenicSpot binarySearchById(int id) {
        // 先按ID排序
        ScenicSpot[] sortedSpots = Arrays.copyOf(spots, size);
        Arrays.sort(sortedSpots, 0, size, Comparator.comparingInt(ScenicSpot::getId));
        
        int index = SearchUtil.binarySearch(sortedSpots, 0, size - 1, 
                                          (spot) -> Integer.compare(spot.getId(), id));
        return index >= 0 ? sortedSpots[index] : null;
    }

    // 按指定条件查找景点
    public SpotList search(Predicate<ScenicSpot> predicate) {
        SpotList result = new SpotList();
        for (int i = 0; i < size; i++) {
            if (predicate.test(spots[i])) {
                result.add(spots[i]);
            }
        }
        return result;
    }

    // 按指定比较器排序
    public void sort(Comparator<ScenicSpot> comparator) {
        if (size > 1) {
            SortUtil.quickSort(spots, 0, size - 1, comparator);
        }
    }

    // 实现Iterable接口
    @Override
    public Iterator<ScenicSpot> iterator() {
        return new Iterator<ScenicSpot>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public ScenicSpot next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return spots[currentIndex++];
            }
        };
    }
}