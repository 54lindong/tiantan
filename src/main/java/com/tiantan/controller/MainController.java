package com.tiantan.controller;

import com.tiantan.model.data.ScenicSpot;
import com.tiantan.model.data.SpotList;
import com.tiantan.model.data.UserPreference;
import com.tiantan.model.graph.ScenicGraph;
import com.tiantan.util.Constants;
import com.tiantan.util.FileUtil;
import com.tiantan.util.LocaleUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * 主界面控制器
 */
public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    @FXML private BorderPane mainPane;
    @FXML private TabPane tabPane;
    @FXML private Tab mapTab;
    @FXML private Tab searchTab;
    @FXML private Tab routeTab;
    @FXML private Tab settingsTab;
    @FXML private Label statusLabel;
    
    // 模型数据
    private SpotList spotList;
    private ScenicGraph scenicGraph;
    private UserPreference userPreference;
    private Properties settings;
    
    // 控制器引用
    private MapController mapController;
    private SearchController searchController;
    private RouteController routeController;
    private SettingsController settingsController;
    
    // 属性绑定
    private final StringProperty statusMessage = new SimpleStringProperty();
    private final ObjectProperty<ScenicSpot> selectedSpot = new SimpleObjectProperty<>();
    private final ObservableList<ScenicSpot> searchResults = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化文件系统
        FileUtil.initDataDirectory();
        
        // 初始化数据模型
        initModels();
        
        // 加载设置
        loadSettings();
        
        // 初始化子界面
        try {
            initTabControllers(resources);
        } catch (IOException e) {
            logger.error("初始化标签页控制器失败", e);
            showStatus(LocaleUtil.getString(Constants.ERROR_DATA_LOAD));
        }
        
        // 绑定属性
        bindProperties();
        
        // 加载数据
        loadData();
        
        logger.info("主界面初始化完成");
    }
    
    /**
     * 初始化数据模型
     */
    private void initModels() {
        spotList = new SpotList();
        scenicGraph = new ScenicGraph(false); // 无向图
        userPreference = new UserPreference();
    }
    
    /**
     * 加载应用程序设置
     */
    private void loadSettings() {
        settings = FileUtil.loadSettings();
        
        // 应用语言设置
        String language = settings.getProperty(Constants.SETTING_LANGUAGE, "zh");
        if ("en".equals(language)) {
            LocaleUtil.switchToEnglish();
        } else {
            LocaleUtil.switchToChinese();
        }
        
        // 更新用户偏好中的语言设置
        userPreference.setEnglish("en".equals(language));
    }
    
    /**
     * 初始化标签页控制器
     */
    private void initTabControllers(ResourceBundle resources) throws IOException {
        // 加载地图标签页
        FXMLLoader mapLoader = new FXMLLoader(getClass().getResource("/fxml/MapView.fxml"), resources);
        mapTab.setContent(mapLoader.load());
        mapController = mapLoader.getController();
        mapController.setMainController(this);
        
        // 加载搜索标签页
        FXMLLoader searchLoader = new FXMLLoader(getClass().getResource("/fxml/SearchView.fxml"), resources);
        searchTab.setContent(searchLoader.load());
        searchController = searchLoader.getController();
        searchController.setMainController(this);
        
        // 加载路线标签页
        FXMLLoader routeLoader = new FXMLLoader(getClass().getResource("/fxml/RoutePlanningView.fxml"), resources);
        routeTab.setContent(routeLoader.load());
        routeController = routeLoader.getController();
        routeController.setMainController(this);
        
        // 加载设置标签页
        FXMLLoader settingsLoader = new FXMLLoader(getClass().getResource("/fxml/SettingsView.fxml"), resources);
        settingsTab.setContent(settingsLoader.load());
        settingsController = settingsLoader.getController();
        settingsController.setMainController(this);
        
        // 更新标签页标题
        updateTabTitles();
    }
    
    /**
     * 更新标签页标题
     */
    private void updateTabTitles() {
        mapTab.setText(LocaleUtil.getString(Constants.KEY_MAP_TAB));
        searchTab.setText(LocaleUtil.getString(Constants.KEY_SEARCH_TAB));
        routeTab.setText(LocaleUtil.getString(Constants.KEY_ROUTE_TAB));
        settingsTab.setText(LocaleUtil.getString(Constants.KEY_SETTINGS_TAB));
    }
    
    /**
     * 绑定属性
     */
    private void bindProperties() {
        statusLabel.textProperty().bind(statusMessage);
        
        // 监听选中景点的变化
        selectedSpot.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mapController.highlightSpot(newValue);
                showStatus(LocaleUtil.getString("status.spotSelected", 
                          newValue.getName(userPreference.isEnglish())));
            }
        });
    }
    
    /**
     * 加载应用数据
     */
    private void loadData() {
        // 加载景点数据
        SpotList loadedSpots = FileUtil.loadScenicSpots();
        for (int i = 0; i < loadedSpots.size(); i++) {
            spotList.add(loadedSpots.get(i));
        }
        
        // 加载景区图数据
        if (!FileUtil.loadScenicGraph(scenicGraph, spotList)) {
            showStatus(LocaleUtil.getString(Constants.ERROR_DATA_LOAD));
        }
        
        // 初始化地图
        mapController.initializeMap(spotList, scenicGraph);
        
        // 初始化搜索
        searchController.initializeSearch(spotList);
        
        // 初始化路线规划
        routeController.initializeRoutePlanning(spotList, scenicGraph);
        
        showStatus(LocaleUtil.getString("status.dataLoaded"));
    }
    
    /**
     * 显示状态信息
     * @param message 状态消息
     */
    public void showStatus(String message) {
        statusMessage.set(message);
    }
    
    /**
 * 切换语言
 * @param isEnglish 是否切换到英文
 */
public void switchLanguage(boolean isEnglish) {
    // 更新语言设置
    if (isEnglish) {
        LocaleUtil.switchToEnglish();
    } else {
        LocaleUtil.switchToChinese();
    }
    
    // 更新用户偏好
    userPreference.setEnglish(isEnglish);
    
    // 保存设置
    settings.setProperty(Constants.SETTING_LANGUAGE, isEnglish ? "en" : "zh");
    FileUtil.saveSettings(settings);
    
    // 更新标签页标题
    updateTabTitles();
    
    // 更新状态栏消息
    showStatus(LocaleUtil.getString("status.languageChanged"));
    
    // 通知用户需要重新启动应用
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
        javafx.scene.control.Alert.AlertType.INFORMATION);
    alert.setTitle(LocaleUtil.getString("language.changed.title"));
    alert.setHeaderText(null);
    alert.setContentText(LocaleUtil.getString("language.changed.message"));
    alert.showAndWait();
}
    
    /**
     * 重新加载界面
     */
    private void reloadUI() throws IOException {
        Stage stage = (Stage) mainPane.getScene().getWindow();
        
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", LocaleUtil.getCurrentLocale());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"), bundle);
        Parent root = loader.load();
        
        Scene scene = new Scene(root, mainPane.getScene().getWidth(), mainPane.getScene().getHeight());
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        stage.setTitle(bundle.getString(Constants.KEY_APP_TITLE));
        stage.setScene(scene);
    }
    
    /**
     * 保存所有数据
     */
    public void saveAllData() {
        if (FileUtil.saveScenicSpots(spotList) && FileUtil.saveScenicGraph(scenicGraph)) {
            showStatus(LocaleUtil.getString("status.dataSaved"));
        } else {
            showStatus(LocaleUtil.getString(Constants.ERROR_DATA_SAVE));
        }
    }
    
    // Getters
    public SpotList getSpotList() {
        return spotList;
    }
    
    public ScenicGraph getScenicGraph() {
        return scenicGraph;
    }
    
    public UserPreference getUserPreference() {
        return userPreference;
    }
    
    public Properties getSettings() {
        return settings;
    }
    
    public ObjectProperty<ScenicSpot> selectedSpotProperty() {
        return selectedSpot;
    }
    
    public ObservableList<ScenicSpot> getSearchResults() {
        return searchResults;
    }
    
    // 选择景点
    public void selectSpot(ScenicSpot spot) {
        selectedSpot.set(spot);
    }
    
    // 切换到指定标签页
    public void switchToTab(int index) {
        tabPane.getSelectionModel().select(index);
    }
}