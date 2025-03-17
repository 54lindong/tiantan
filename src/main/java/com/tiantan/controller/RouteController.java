package com.tiantan.controller;

import com.tiantan.model.algorithm.MST;
import com.tiantan.model.algorithm.ShortestPath;
import com.tiantan.model.data.Route;
import com.tiantan.model.data.RouteStop;
import com.tiantan.model.data.RouteType;
import com.tiantan.model.data.ScenicSpot;
import com.tiantan.model.data.SpotList;
import com.tiantan.model.graph.Edge;
import com.tiantan.model.graph.ScenicGraph;
import com.tiantan.model.graph.Vertex;
import com.tiantan.util.Constants;
import com.tiantan.util.LocaleUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 路线规划控制器
 */
public class RouteController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
    
    @FXML private ComboBox<ScenicSpot> startSpotComboBox;
    @FXML private ComboBox<ScenicSpot> endSpotComboBox;
    @FXML private ComboBox<RouteType> routeTypeComboBox;
    @FXML private Spinner<Integer> timeSpinner;
    @FXML private CheckBox avoidCrowdsCheckBox;
    @FXML private CheckBox accessibleOnlyCheckBox;
    @FXML private Button findRouteButton;
    @FXML private Button clearButton;
    @FXML private TableView<RouteStop> routeTable;
    @FXML private TableColumn<RouteStop, String> orderColumn;
    @FXML private TableColumn<RouteStop, String> spotNameColumn;
    @FXML private TableColumn<RouteStop, String> stayTimeColumn;
    @FXML private Button moveUpButton;
    @FXML private Button moveDownButton;
    @FXML private Button removeStopButton;
    @FXML private Label totalTimeLabel;
    @FXML private Label totalDistanceLabel;
    @FXML private VBox routeInfoBox;
    @FXML private Label routeNameLabel;
    @FXML private TextArea routeDescriptionArea;
    @FXML private Button saveRouteButton;
    @FXML private Button optimizeRouteButton;
    @FXML private ListView<ScenicSpot> availableSpotsListView;
    @FXML private Button addToRouteButton;
    @FXML private CheckBox preferPopularCheckBox;
    
    private MainController mainController;
    private SpotList spotList;
    private ScenicGraph scenicGraph;
    private Route currentRoute;
    private ObservableList<RouteStop> routeStops = FXCollections.observableArrayList();
    private ObservableList<ScenicSpot> availableSpots = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化控件
        initControls();
        
        // 设置事件处理
        initEventHandlers();
        
        // 初始化路线表格
        initRouteTable();
        
        // 默认隐藏路线信息面板
        routeInfoBox.setVisible(false);
    }
    
    /**
     * 设置主控制器
     * @param controller 主控制器
     */
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }
    
    /**
     * 初始化路线规划
     * @param spotList 景点列表
     * @param scenicGraph 景区图
     */
    public void initializeRoutePlanning(SpotList spotList, ScenicGraph scenicGraph) {
        this.spotList = spotList;
        this.scenicGraph = scenicGraph;
        
        // 初始化控件数据
        loadSpotComboBoxes();
        updateAvailableSpotsList();
    }
    
    
    /**
     * 初始化控件
     */
    private void initControls() {
        // 初始化时间选择器
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(30, 360, 120, 30);
        timeSpinner.setValueFactory(valueFactory);
        
        // 初始化路线类型下拉框
        routeTypeComboBox.setItems(FXCollections.observableArrayList(RouteType.values()));
        routeTypeComboBox.getSelectionModel().selectFirst();
        
        // 设置路线类型显示
        routeTypeComboBox.setConverter(new javafx.util.StringConverter<RouteType>() {
            @Override
            public String toString(RouteType type) {
                return type == null ? "" : type.getName(mainController.getUserPreference().isEnglish());
            }
            
            @Override
            public RouteType fromString(String string) {
                return null; // 不需要从字符串转换
            }
        });
    }
    
    /**
     * 初始化事件处理
     */
    private void initEventHandlers() {
        // 查找路线按钮
        findRouteButton.setOnAction(e -> findRoute());
        
        // 清除按钮
        clearButton.setOnAction(e -> clearRoute());
        
        // 移动停留点按钮
        moveUpButton.setOnAction(e -> moveStopUp());
        moveDownButton.setOnAction(e -> moveStopDown());
        
        // 删除停留点按钮
        removeStopButton.setOnAction(e -> removeSelectedStop());
        
        // 保存路线按钮
        saveRouteButton.setOnAction(e -> saveCurrentRoute());
        
        // 优化路线按钮
        optimizeRouteButton.setOnAction(e -> optimizeRoute());
        
        // 添加到路线按钮
        addToRouteButton.setOnAction(e -> addSelectedSpotToRoute());
        
        // 偏好设置变化事件
        preferPopularCheckBox.setOnAction(e -> updateAvailableSpotsList());
    }
    
    /**
     * 初始化路线表格
     */
    private void initRouteTable() {
        // 设置序号列
        orderColumn.setCellValueFactory(cellData -> {
            int index = routeStops.indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });
        
        // 设置景点名称列
        spotNameColumn.setCellValueFactory(cellData -> {
            ScenicSpot spot = cellData.getValue().getSpot();
            boolean isEnglish = mainController.getUserPreference().isEnglish();
            return new SimpleStringProperty(spot.getName(isEnglish));
        });
        
        // 设置停留时间列
        stayTimeColumn.setCellValueFactory(cellData -> {
            Duration duration = cellData.getValue().getStayDuration();
            return new SimpleStringProperty(duration.toMinutes() + " " + 
                                          LocaleUtil.getString("time.minutes"));
        });
        
        // 绑定数据
        routeTable.setItems(routeStops);
        
        // 选择事件
        routeTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                boolean hasSelection = newValue != null;
                moveUpButton.setDisable(!hasSelection);
                moveDownButton.setDisable(!hasSelection);
                removeStopButton.setDisable(!hasSelection);
            }
        );
    }
    
    /**
     * 加载景点下拉框
     */
    private void loadSpotComboBoxes() {
        // 获取景点列表
        ObservableList<ScenicSpot> spots = FXCollections.observableArrayList();
        for (int i = 0; i < spotList.size(); i++) {
            spots.add(spotList.get(i));
        }
        
        // 设置下拉框
        startSpotComboBox.setItems(spots);
        endSpotComboBox.setItems(spots);
        
        // 设置显示转换器
        javafx.util.StringConverter<ScenicSpot> converter = new javafx.util.StringConverter<ScenicSpot>() {
            @Override
            public String toString(ScenicSpot spot) {
                if (spot == null) return "";
                boolean isEnglish = mainController.getUserPreference().isEnglish();
                return spot.getName(isEnglish);
            }
            
            @Override
            public ScenicSpot fromString(String string) {
                return null; // 不需要从字符串转换
            }
        };
        
        startSpotComboBox.setConverter(converter);
        endSpotComboBox.setConverter(converter);
        
        // 默认选择东门和祈年殿
        startSpotComboBox.getSelectionModel().select(findSpotById(9)); // 东门ID
        endSpotComboBox.getSelectionModel().select(findSpotById(1));   // 祈年殿ID
    }
    
    /**
     * 根据ID设置起点和终点
     * @param startId 起点ID
     * @param endId 终点ID
     */
    public void setStartAndEndById(int startId, int endId) {
        ScenicSpot startSpot = findSpotById(startId);
        ScenicSpot endSpot = findSpotById(endId);
        
        if (startSpot != null) {
            startSpotComboBox.setValue(startSpot);
        }
        
        if (endSpot != null) {
            endSpotComboBox.setValue(endSpot);
        }
    }
    
    /**
     * 更新可用景点列表
     */
    private void updateAvailableSpotsList() {
        // 清除现有列表
        availableSpots.clear();
        
        // 添加所有景点
        for (int i = 0; i < spotList.size(); i++) {
            ScenicSpot spot = spotList.get(i);
            // 排除已在路线中的景点
            boolean alreadyInRoute = routeStops.stream()
                .anyMatch(stop -> stop.getSpot().equals(spot));
            
            if (!alreadyInRoute) {
                availableSpots.add(spot);
            }
        }
        
        // 如果选择了优先热门景点，则按热门程度排序
        if (preferPopularCheckBox.isSelected()) {
            availableSpots.sort((s1, s2) -> Integer.compare(s2.getPopularity(), s1.getPopularity()));
        }
        
        // 更新列表视图
        availableSpotsListView.setItems(availableSpots);
        
        // 设置单元格工厂
        availableSpotsListView.setCellFactory(param -> new ListCell<ScenicSpot>() {
            @Override
            protected void updateItem(ScenicSpot spot, boolean empty) {
                super.updateItem(spot, empty);
                if (empty || spot == null) {
                    setText(null);
                } else {
                    boolean isEnglish = mainController.getUserPreference().isEnglish();
                    setText(spot.getName(isEnglish) + " (" + spot.getVisitTime() + "分钟)");
                }
            }
        });
    }
    
    /**
     * 查找路线
     */
    @FXML
    private void findRoute() {
        // 获取选择的起点和终点
        ScenicSpot startSpot = startSpotComboBox.getValue();
        ScenicSpot endSpot = endSpotComboBox.getValue();
        
        if (startSpot == null || endSpot == null) {
            showAlert(Alert.AlertType.WARNING, 
                    LocaleUtil.getString("route.error.noSpotSelected"),
                    LocaleUtil.getString("route.error.pleaseSelectSpots"));
            return;
        }
        
        // 获取其他选项
        int timeLimit = timeSpinner.getValue();
        boolean avoidCrowds = avoidCrowdsCheckBox.isSelected();
        boolean accessibleOnly = accessibleOnlyCheckBox.isSelected();
        RouteType routeType = routeTypeComboBox.getValue();
        
        // 定义权重函数
        Function<Edge, Double> weightFunction;
        if (avoidCrowds) {
            weightFunction = Edge::getEffectiveWeight;
        } else {
            weightFunction = Edge::getWeight;
        }
        
        // 计算最短路径
        Map<Integer, Integer> predecessor = ShortestPath.dijkstra(
            scenicGraph, startSpot.getId(), weightFunction);
        List<ScenicSpot> pathSpots = ShortestPath.constructPath(
            scenicGraph, predecessor, endSpot.getId());
        
        if (pathSpots.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, 
                    LocaleUtil.getString("route.error.noPath"),
                    LocaleUtil.getString("route.error.cannotFindPath"));
            return;
        }
        
        // 创建新路线
        String nameZh = startSpot.getNameZh() + " 到 " + endSpot.getNameZh() + " 路线";
        String nameEn = "Route from " + startSpot.getNameEn() + " to " + endSpot.getNameEn();
        
        currentRoute = new Route(
            generateRouteId(),
            nameZh,
            nameEn,
            "从" + startSpot.getNameZh() + "出发，途经景区主要景点，最终到达" + endSpot.getNameZh() + "的游览路线。",
            "A touring route from " + startSpot.getNameEn() + ", passing through main attractions, and finally arriving at " + endSpot.getNameEn() + ".",
            routeType,
            !accessibleOnly // 如果要求无障碍，则检查路径上的所有景点
        );
        
        // 将路径上的景点添加到路线中
        routeStops.clear();
        int totalTime = 0;
        
        for (ScenicSpot spot : pathSpots) {
            // 检查无障碍要求
            if (accessibleOnly && !spot.isAccessible()) {
                continue;
            }
            
            // 添加景点到路线
            RouteStop stop = new RouteStop(spot);
            routeStops.add(stop);
            currentRoute.addStop(stop);
            
            // 累计时间
            totalTime += spot.getVisitTime();
            
            // 如果总时间超过限制，停止添加
            if (totalTime > timeLimit) {
                break;
            }
        }
        
        // 更新路线信息
        updateRouteInfo();
        
        // 显示路线信息面板
        routeInfoBox.setVisible(true);
        
        // 更新可用景点列表
        updateAvailableSpotsList();
        
        logger.info("路线规划完成，起点: {}, 终点: {}, 景点数: {}", 
                  startSpot.getNameZh(), endSpot.getNameZh(), routeStops.size());
    }
    
    /**
     * 清除路线
     */
    @FXML
    private void clearRoute() {
        // 清除路线数据
        routeStops.clear();
        currentRoute = null;
        
        // 隐藏路线信息面板
        routeInfoBox.setVisible(false);
        
        // 清除总时间和距离标签
        totalTimeLabel.setText("");
        totalDistanceLabel.setText("");
        
        // 重置控件状态
        moveUpButton.setDisable(true);
        moveDownButton.setDisable(true);
        removeStopButton.setDisable(true);
        
        // 更新可用景点列表
        updateAvailableSpotsList();
    }
    
    /**
     * 上移选中的停留点
     */
    @FXML
    private void moveStopUp() {
        int selectedIndex = routeTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            // 交换位置
            RouteStop stop = routeStops.remove(selectedIndex);
            routeStops.add(selectedIndex - 1, stop);
            
            // 更新当前路线
            if (currentRoute != null) {
                currentRoute.swapStops(selectedIndex, selectedIndex - 1);
            }
            
            // 更新选中项
            routeTable.getSelectionModel().select(selectedIndex - 1);
            
            // 更新路线信息
            updateRouteInfo();
        }
    }
    
    /**
     * 下移选中的停留点
     */
    @FXML
    private void moveStopDown() {
        int selectedIndex = routeTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < routeStops.size() - 1) {
            // 交换位置
            RouteStop stop = routeStops.remove(selectedIndex);
            routeStops.add(selectedIndex + 1, stop);
            
            // 更新当前路线
            if (currentRoute != null) {
                currentRoute.swapStops(selectedIndex, selectedIndex + 1);
            }
            
            // 更新选中项
            routeTable.getSelectionModel().select(selectedIndex + 1);
            
            // 更新路线信息
            updateRouteInfo();
        }
    }
    
    /**
     * 删除选中的停留点
     */
    @FXML
    private void removeSelectedStop() {
        int selectedIndex = routeTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            // 移除停留点
            RouteStop removedStop = routeStops.remove(selectedIndex);
            
            // 更新当前路线
            if (currentRoute != null) {
                currentRoute.removeStop(selectedIndex);
            }
            
            // 更新路线信息
            updateRouteInfo();
            
            // 更新可用景点列表
            updateAvailableSpotsList();
            
            // 如果路线为空，隐藏路线信息面板
            if (routeStops.isEmpty()) {
                routeInfoBox.setVisible(false);
            }
        }
    }
    
    /**
     * 保存当前路线
     */
    @FXML
    private void saveCurrentRoute() {
        if (currentRoute == null || routeStops.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, 
                    LocaleUtil.getString("route.error.noRoute"),
                    LocaleUtil.getString("route.error.pleaseCreateRoute"));
            return;
        }
        
        // TODO: 实现路线保存功能
        
        // 显示成功消息
        mainController.showStatus(LocaleUtil.getString("status.routeSaved"));
    }
    
    /**
     * 优化路线
     */
    @FXML
    private void optimizeRoute() {
        if (routeStops.size() < 3) {
            showAlert(Alert.AlertType.INFORMATION, 
                    LocaleUtil.getString("route.info.cannotOptimize"),
                    LocaleUtil.getString("route.info.needMoreSpots"));
            return;
        }
        
        // 获取当前路线上的所有景点
        List<ScenicSpot> spots = routeStops.stream()
            .map(RouteStop::getSpot)
            .collect(Collectors.toList());
        
        // 创建子图
        ScenicGraph subGraph = createSubGraphForSpots(spots);
        
        // 使用近似TSP算法优化路线
        List<ScenicSpot> optimizedPath = MST.approximateTSP(
            subGraph, spots.get(0).getId(), Edge::getWeight);
        
        if (optimizedPath.size() < spots.size()) {
            showAlert(Alert.AlertType.WARNING, 
                    LocaleUtil.getString("route.warning.partialOptimization"),
                    LocaleUtil.getString("route.warning.someSpotsMayBeUnreachable"));
        }
        
        // 更新路线
        routeStops.clear();
        for (ScenicSpot spot : optimizedPath) {
            routeStops.add(new RouteStop(spot));
        }
        
        // 重新创建路线对象
        if (currentRoute != null) {
            RouteType routeType = currentRoute.getType();
            currentRoute = new Route(
                generateRouteId(),
                currentRoute.getNameZh() + " (优化)",
                currentRoute.getNameEn() + " (Optimized)",
                "优化后的路线，按照最佳游览顺序排列景点。",
                "Optimized route with attractions arranged in the best visiting order.",
                routeType,
                currentRoute.isAccessible()
            );
            
            // 添加停留点
            for (RouteStop stop : routeStops) {
                currentRoute.addStop(stop);
            }
        }
        
        // 更新路线信息
        updateRouteInfo();
    }
    
    /**
     * 添加选中的景点到路线
     */
    @FXML
    private void addSelectedSpotToRoute() {
        ScenicSpot selectedSpot = availableSpotsListView.getSelectionModel().getSelectedItem();
        if (selectedSpot == null) {
            showAlert(Alert.AlertType.WARNING, 
                    LocaleUtil.getString("route.error.noSpotSelected"),
                    LocaleUtil.getString("route.error.pleaseSelectSpotToAdd"));
            return;
        }
        
        // 如果当前没有路线，创建一个新路线
        if (currentRoute == null) {
            currentRoute = new Route(
                generateRouteId(),
                "自定义路线",
                "Custom Route",
                "用户自定义的游览路线。",
                "A custom touring route created by the user.",
                RouteType.CLASSIC,
                selectedSpot.isAccessible()
            );
            routeInfoBox.setVisible(true);
        }
        
        // 创建并添加停留点
        RouteStop newStop = new RouteStop(selectedSpot);
        routeStops.add(newStop);
        currentRoute.addStop(newStop);
        
        // 更新路线信息
        updateRouteInfo();
        
        // 更新可用景点列表
        updateAvailableSpotsList();
    }
    
    /**
     * 更新路线信息
     */
    private void updateRouteInfo() {
        if (currentRoute == null) return;
        
        boolean isEnglish = mainController.getUserPreference().isEnglish();
        
        // 更新路线名称和描述
        routeNameLabel.setText(currentRoute.getName(isEnglish));
        routeDescriptionArea.setText(currentRoute.getDescription(isEnglish));
        
        // 更新总时间和距离
        Duration totalTime = currentRoute.getEstimatedDuration();
        double totalDistance = currentRoute.getTotalDistance();
        
        totalTimeLabel.setText(LocaleUtil.getString("route.totalTime") + ": " + 
                             totalTime.toHours() + "小时" + (totalTime.toMinutesPart()) + "分钟");
        
        totalDistanceLabel.setText(LocaleUtil.getString("route.totalDistance") + ": " + 
                                 String.format("%.1f 米", totalDistance));
    }
    
    /**
     * 为指定景点集合创建子图
     * @param spots 景点列表
     * @return 包含这些景点的子图
     */
    private ScenicGraph createSubGraphForSpots(List<ScenicSpot> spots) {
        ScenicGraph subGraph = new ScenicGraph(false);
        
        // 添加顶点
        for (ScenicSpot spot : spots) {
            subGraph.addVertex(spot);
        }
        
        // 添加边
        for (ScenicSpot from : spots) {
            for (ScenicSpot to : spots) {
                if (!from.equals(to)) {
                    // 查找原图中的边
                    Edge edge = scenicGraph.getVertex(from.getId()).getEdgeTo(to.getId());
                    if (edge != null) {
                        subGraph.addEdge(from.getId(), to.getId(), edge.getWeight(), edge.getType());
                    } else {
                        // 如果原图中没有直接连接，可以添加一个权重较大的边
                        double distance = from.distanceTo(to);
                        subGraph.addEdge(from.getId(), to.getId(), distance, com.tiantan.model.graph.EdgeType.WALKING);
                    }
                }
            }
        }
        
        return subGraph;
    }
    
    /**
     * 生成路线ID
     * @return 新路线ID
     */
    private int generateRouteId() {
        // 简单实现，实际应用中可能需要更复杂的ID生成策略
        return (int) (System.currentTimeMillis() % 10000);
    }
    
    /**
     * 查找指定ID的景点
     * @param id 景点ID
     * @return 景点对象，如果不存在返回null
     */
    private ScenicSpot findSpotById(int id) {
        for (int i = 0; i < spotList.size(); i++) {
            ScenicSpot spot = spotList.get(i);
            if (spot.getId() == id) {
                return spot;
            }
        }
        return null;
    }
    
    /**
     * 显示警告对话框
     * @param alertType 警告类型
     * @param title 标题
     * @param message 消息内容
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
