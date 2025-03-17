package com.tiantan.controller;

import com.tiantan.model.algorithm.SearchUtil;
import com.tiantan.model.data.ScenicSpot;
import com.tiantan.model.data.SpotList;
import com.tiantan.util.Constants;
import com.tiantan.util.LocaleUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * 搜索控制器
 */
public class SearchController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private CheckBox accessibleCheckBox;
    @FXML private Button searchButton;
    @FXML private TableView<ScenicSpot> resultsTable;
    @FXML private TableColumn<ScenicSpot, String> nameColumn;
    @FXML private TableColumn<ScenicSpot, String> categoryColumn;
    @FXML private TableColumn<ScenicSpot, String> visitTimeColumn;
    @FXML private Label resultCountLabel;
    @FXML private Button clearButton;
    @FXML private Button sortByNameButton;
    @FXML private Button sortByPopularityButton;
    @FXML private Button sortByVisitTimeButton;
    @FXML private CheckBox fuzzySearchCheckBox;
    
    private MainController mainController;
    private SpotList spotList;
    private ObservableList<ScenicSpot> searchResults = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化分类下拉框
        initCategoryComboBox();
        
        // 初始化表格
        initResultsTable();
        
        // 设置事件处理
        initEventHandlers();
    }
    
    /**
     * 设置主控制器
     * @param controller 主控制器
     */
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }
    
    /**
     * 初始化搜索
     * @param spotList 景点列表
     */
    public void initializeSearch(SpotList spotList) {
        this.spotList = spotList;
        
        // 加载所有景点到结果表格中
        searchResults.clear();
        for (int i = 0; i < spotList.size(); i++) {
            searchResults.add(spotList.get(i));
        }
        
        // 更新结果计数
        updateResultCount();
    }
    
    /**
     * 初始化分类下拉框
     */
    private void initCategoryComboBox() {
        // 添加"全部"选项
        categoryComboBox.getItems().add(LocaleUtil.getString("search.allCategories"));
        
        // 添加所有景点分类
        for (String category : Constants.SPOT_CATEGORIES) {
            categoryComboBox.getItems().add(category);
        }
        
        // 默认选择"全部"
        categoryComboBox.getSelectionModel().selectFirst();
    }
    
    /**
     * 初始化结果表格
     */
    private void initResultsTable() {
        // 设置表格列
        nameColumn.setCellValueFactory(cellData -> {
            ScenicSpot spot = cellData.getValue();
            boolean isEnglish = mainController.getUserPreference().isEnglish();
            return new SimpleStringProperty(spot.getName(isEnglish));
        });
        
        categoryColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory()));
        
        visitTimeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getVisitTime() + " " + 
                                   LocaleUtil.getString("time.minutes")));
        
        // 绑定数据
        resultsTable.setItems(searchResults);
        
        // 双击行选择景点
        resultsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ScenicSpot selectedSpot = resultsTable.getSelectionModel().getSelectedItem();
                if (selectedSpot != null) {
                    mainController.selectSpot(selectedSpot);
                    mainController.switchToTab(0); // 切换到地图标签页
                }
            }
        });
    }
    
    /**
     * 初始化事件处理
     */
    private void initEventHandlers() {
        // 搜索按钮点击事件
        searchButton.setOnAction(e -> performSearch());
        
        // 回车键搜索
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                performSearch();
            }
        });
        
        // 清除按钮点击事件
        clearButton.setOnAction(e -> clearSearch());
        
        // 排序按钮点击事件
        sortByNameButton.setOnAction(e -> sortResults(SortType.BY_NAME));
        sortByPopularityButton.setOnAction(e -> sortResults(SortType.BY_POPULARITY));
        sortByVisitTimeButton.setOnAction(e -> sortResults(SortType.BY_VISIT_TIME));
    }
    
    /**
     * 执行搜索
     */
    @FXML
    private void performSearch() {
        String searchText = searchField.getText().trim();
        String selectedCategory = categoryComboBox.getValue();
        boolean accessibleOnly = accessibleCheckBox.isSelected();
        boolean fuzzySearch = fuzzySearchCheckBox.isSelected();
        boolean isEnglish = mainController.getUserPreference().isEnglish();
        
        // 创建搜索条件
        Predicate<ScenicSpot> searchPredicate = spot -> {
            // 名称匹配
            boolean nameMatch;
            if (fuzzySearch) {
                // 模糊匹配
                String spotName = spot.getName(isEnglish);
                nameMatch = searchText.isEmpty() || 
                           SearchUtil.levenshteinDistance(searchText.toLowerCase(), 
                                                       spotName.toLowerCase()) <= Constants.SEARCH_FUZZY_THRESHOLD;
            } else {
                // 精确匹配（包含）
                String spotName = spot.getName(isEnglish);
                nameMatch = searchText.isEmpty() || 
                           spotName.toLowerCase().contains(searchText.toLowerCase());
            }
            
            // 分类匹配
            boolean categoryMatch = selectedCategory.equals(LocaleUtil.getString("search.allCategories")) || 
                                  spot.getCategory().equals(selectedCategory);
            
            // 无障碍匹配
            boolean accessibleMatch = !accessibleOnly || spot.isAccessible();
            
            return nameMatch && categoryMatch && accessibleMatch;
        };
        
        // 使用线性表的搜索功能
        SpotList filteredList = spotList.search(searchPredicate);
        
        // 更新结果列表
        searchResults.clear();
        for (int i = 0; i < filteredList.size(); i++) {
            searchResults.add(filteredList.get(i));
        }
        
        // 更新结果计数
        updateResultCount();
        
        logger.info("搜索完成，关键词: {}, 分类: {}, 结果数: {}", 
                  searchText, selectedCategory, searchResults.size());
    }
    
    /**
     * 清除搜索
     */
    @FXML
    private void clearSearch() {
        searchField.clear();
        categoryComboBox.getSelectionModel().selectFirst();
        accessibleCheckBox.setSelected(false);
        fuzzySearchCheckBox.setSelected(false);
        
        // 显示所有景点
        searchResults.clear();
        for (int i = 0; i < spotList.size(); i++) {
            searchResults.add(spotList.get(i));
        }
        
        // 更新结果计数
        updateResultCount();
    }
    
    /**
     * 排序类型枚举
     */
    private enum SortType {
        BY_NAME, BY_POPULARITY, BY_VISIT_TIME
    }
    
    /**
     * 排序搜索结果
     * @param sortType 排序类型
     */
    private void sortResults(SortType sortType) {
        boolean isEnglish = mainController.getUserPreference().isEnglish();
        
        // 根据排序类型创建比较器
        Comparator<ScenicSpot> comparator;
        switch (sortType) {
            case BY_NAME:
                comparator = (s1, s2) -> s1.getName(isEnglish).compareTo(s2.getName(isEnglish));
                break;
            case BY_POPULARITY:
                comparator = (s1, s2) -> Integer.compare(s2.getPopularity(), s1.getPopularity());
                break;
            case BY_VISIT_TIME:
                comparator = Comparator.comparingInt(ScenicSpot::getVisitTime);
                break;
            default:
                comparator = (s1, s2) -> s1.getName(isEnglish).compareTo(s2.getName(isEnglish));
        }
        
        // 转换为数组进行排序
        ScenicSpot[] spotsArray = searchResults.toArray(new ScenicSpot[0]);
        if (spotsArray.length > 1) {
            // 使用快速排序
            com.tiantan.model.algorithm.SortUtil.quickSort(spotsArray, 0, spotsArray.length - 1, comparator);
            
            // 更新表格数据
            searchResults.clear();
            searchResults.addAll(spotsArray);
        }
    }
    
    /**
     * 更新结果计数标签
     */
    private void updateResultCount() {
        resultCountLabel.setText(LocaleUtil.getString("search.resultCount", searchResults.size()));
    }
    
    /**
     * 根据景点ID查找景点
     * @param spotId 景点ID
     * @return 找到的景点，如果未找到返回null
     */
    public ScenicSpot findSpotById(int spotId) {
        return spotList.binarySearchById(spotId);
    }
    
    /**
     * 获取搜索结果
     * @return 搜索结果列表
     */
    public ObservableList<ScenicSpot> getSearchResults() {
        return searchResults;
    }
}