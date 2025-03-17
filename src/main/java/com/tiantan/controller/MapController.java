package com.tiantan.controller;

import com.tiantan.model.algorithm.MST;
import com.tiantan.model.algorithm.ShortestPath;
import com.tiantan.model.data.ScenicSpot;
import com.tiantan.model.data.SpotList;
import com.tiantan.model.graph.Edge;
import com.tiantan.model.graph.ScenicGraph;
import com.tiantan.model.graph.Vertex;
import com.tiantan.util.Constants;
import com.tiantan.util.LocaleUtil;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * 地图控制器
 */
public class MapController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MapController.class);
    
    // FXML元素
    @FXML private AnchorPane mapContainer;
    @FXML private Pane mapPane;
    @FXML private Slider zoomSlider;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button resetButton;
    @FXML private ComboBox<String> viewModeComboBox;
    @FXML private ToggleButton showPathsToggle;
    @FXML private ToggleButton showLabelsToggle;
    @FXML private CheckBox avoidCrowdsCheckBox;
    @FXML private VBox spotInfoBox;
    @FXML private Label spotNameLabel;
    @FXML private TextArea spotDescriptionText;
    @FXML private Label spotCategoryLabel;
    @FXML private Label spotVisitTimeLabel;
    @FXML private Button findRouteToButton;
    @FXML private Button centerMapButton;
    
    // 非FXML元素
    private Image mapImage;
    private ImageView mapImageView;
    private ToggleGroup spotToggleGroup;
    
    // 模型数据
    private MainController mainController;
    private SpotList spotList;
    private ScenicGraph scenicGraph;
    
    // 地图状态
    private final DoubleProperty zoomLevel = new SimpleDoubleProperty(Constants.MAP_DEFAULT_ZOOM);
    private double dragStartX, dragStartY;
    private ScenicSpot selectedSpot;
    private Map<Integer, Circle> spotCircles = new HashMap<>();
    private List<Line> pathLines = new ArrayList<>();
    private Map<Integer, Label> spotLabels = new HashMap<>();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化地图背景
        initMapBackground();
        
        // 初始化控件
        initControls();
        
        // 绑定属性
        bindProperties();
        
        // 初始化事件处理
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
     * 初始化地图
     * @param spotList 景点列表
     * @param scenicGraph 景区图
     */
    public void initializeMap(SpotList spotList, ScenicGraph scenicGraph) {
        this.spotList = spotList;
        this.scenicGraph = scenicGraph;
        
        // 更新景点显示
        updateSpotDisplay();
        
        // 显示路径
        if (showPathsToggle.isSelected()) {
            showPaths();
        }
    }
    
    /**
     * 初始化地图背景
     */
    private void initMapBackground() {
    try {
        mapImage = new Image(getClass().getResourceAsStream("/images/tiantan_map.jpg"));
        
        if (mapImage.isError()) {
            throw new IOException("地图图像加载错误: " + mapImage.getException().getMessage());
        }
        
        mapImageView = new ImageView(mapImage);
        mapImageView.setPreserveRatio(true);
        mapPane.getChildren().add(mapImageView);
        
        // 设置初始大小
        mapImageView.setFitWidth(mapImage.getWidth() * Constants.MAP_DEFAULT_ZOOM);
        mapImageView.setFitHeight(mapImage.getHeight() * Constants.MAP_DEFAULT_ZOOM);
        
        logger.info("地图背景加载成功");
    } catch (Exception e) {
        logger.error("加载地图图像失败", e);
        
        // 加载失败时显示一个占位图
        try {
            mapImage = new Image(getClass().getResourceAsStream("/images/map_placeholder.jpg"));
            if (mapImage.isError()) {
                createEmptyMap(); // 如果占位图也加载失败，创建一个空白地图
            } else {
                mapImageView = new ImageView(mapImage);
                mapImageView.setPreserveRatio(true);
                mapPane.getChildren().add(mapImageView);
                mapImageView.setFitWidth(mapImage.getWidth() * Constants.MAP_DEFAULT_ZOOM);
                mapImageView.setFitHeight(mapImage.getHeight() * Constants.MAP_DEFAULT_ZOOM);
            }
        } catch (Exception ex) {
            logger.error("加载占位图失败", ex);
            createEmptyMap();
        }
    }
}
    /**
 * 创建空白地图
 */
private void createEmptyMap() {
    javafx.scene.shape.Rectangle emptyMap = new javafx.scene.shape.Rectangle(800, 600);
    emptyMap.setFill(javafx.scene.paint.Color.LIGHTGRAY);
    mapPane.getChildren().add(emptyMap);
    
    javafx.scene.text.Text text = new javafx.scene.text.Text("地图加载失败");
    text.setFont(javafx.scene.text.Font.font(24));
    text.setFill(javafx.scene.paint.Color.RED);
    text.setX(350);
    text.setY(300);
    mapPane.getChildren().add(text);
    
    logger.warn("创建了空白地图");
}
    /**
     * 初始化控件
     */
    private void initControls() {
        // 设置缩放滑块范围
        zoomSlider.setMin(Constants.MAP_MIN_ZOOM);
        zoomSlider.setMax(Constants.MAP_MAX_ZOOM);
        zoomSlider.setValue(Constants.MAP_DEFAULT_ZOOM);
        
        // 初始化视图模式选项
        viewModeComboBox.getItems().addAll(
            LocaleUtil.getString("map.viewMode.normal"),
            LocaleUtil.getString("map.viewMode.satellite"),
            LocaleUtil.getString("map.viewMode.schematic")
        );
        viewModeComboBox.getSelectionModel().selectFirst();
        
        // 初始化景点选择组
        spotToggleGroup = new ToggleGroup();
        
        // 默认显示标签
        showLabelsToggle.setSelected(true);
        
        // 默认隐藏景点信息面板
        spotInfoBox.setVisible(false);
    }
    
    /**
     * 属性绑定
     */
    private void bindProperties() {
        // 绑定缩放属性
        zoomLevel.bindBidirectional(zoomSlider.valueProperty());
        
        // 绑定缩放到地图大小
        mapImageView.fitWidthProperty().bind(
            zoomLevel.multiply(mapImage.getWidth())
        );
        mapImageView.fitHeightProperty().bind(
            zoomLevel.multiply(mapImage.getHeight())
        );
    }
    
    /**
     * 初始化事件处理
     */
    private void initEventHandlers() {
        // 地图拖拽
        mapPane.setOnMousePressed(this::handleMapPressed);
        mapPane.setOnMouseDragged(this::handleMapDragged);
        
        // 缩放按钮
        zoomInButton.setOnAction(e -> zoomIn());
        zoomOutButton.setOnAction(e -> zoomOut());
        resetButton.setOnAction(e -> resetView());
        
        // 显示模式切换
        viewModeComboBox.setOnAction(e -> updateMapStyle());
        
        // 路径和标签显示切换
        showPathsToggle.setOnAction(e -> togglePaths());
        showLabelsToggle.setOnAction(e -> toggleLabels());
        
        // 避开拥挤区域
        avoidCrowdsCheckBox.setOnAction(e -> {
            if (selectedSpot != null) {
                showPathToSelectedSpot();
            }
        });
        
        // 景点信息按钮
        findRouteToButton.setOnAction(e -> {
            if (selectedSpot != null) {
                mainController.switchToTab(2); // 切换到路线规划标签页
                // TODO: 设置路线规划起点/终点
            }
        });
        
        centerMapButton.setOnAction(e -> {
            if (selectedSpot != null) {
                centerOnSpot(selectedSpot);
            }
        });
    }
    
    /**
     * 处理地图点击
     */
    private void handleMapPressed(MouseEvent event) {
        dragStartX = event.getX();
        dragStartY = event.getY();
        mapPane.setCursor(Cursor.CLOSED_HAND);
    }
    
    /**
     * 处理地图拖拽
     */
    private void handleMapDragged(MouseEvent event) {
        double offsetX = event.getX() - dragStartX;
        double offsetY = event.getY() - dragStartY;
        
        // 更新地图位置
        double newLayoutX = mapPane.getLayoutX() + offsetX;
        double newLayoutY = mapPane.getLayoutY() + offsetY;
        
        mapPane.setLayoutX(newLayoutX);
        mapPane.setLayoutY(newLayoutY);
        
        dragStartX = event.getX();
        dragStartY = event.getY();
    }
    
    /**
     * 放大地图
     */
    @FXML
    private void zoomIn() {
        double newZoom = zoomLevel.get() + Constants.MAP_ZOOM_STEP;
        if (newZoom <= Constants.MAP_MAX_ZOOM) {
            zoomLevel.set(newZoom);
        }
    }
    
    /**
     * 缩小地图
     */
    @FXML
    private void zoomOut() {
        double newZoom = zoomLevel.get() - Constants.MAP_ZOOM_STEP;
        if (newZoom >= Constants.MAP_MIN_ZOOM) {
            zoomLevel.set(newZoom);
        }
    }
    
    /**
     * 重置视图
     */
    @FXML
    private void resetView() {
        zoomLevel.set(Constants.MAP_DEFAULT_ZOOM);
        mapPane.setLayoutX(0);
        mapPane.setLayoutY(0);
    }
    
    /**
     * 更新地图样式
     */
    private void updateMapStyle() {
        String selectedMode = viewModeComboBox.getSelectionModel().getSelectedItem();
        String styleClass = "";
        
        if (selectedMode.equals(LocaleUtil.getString("map.viewMode.satellite"))) {
            styleClass = "satellite-map";
        } else if (selectedMode.equals(LocaleUtil.getString("map.viewMode.schematic"))) {
            styleClass = "schematic-map";
        } else {
            styleClass = "normal-map";
        }
        
        // 清除旧样式
        mapPane.getStyleClass().removeAll("normal-map", "satellite-map", "schematic-map");
        mapPane.getStyleClass().add(styleClass);
    }
    
    /**
     * 切换路径显示
     */
    private void togglePaths() {
        if (showPathsToggle.isSelected()) {
            showPaths();
        } else {
            hidePaths();
        }
    }
    
    /**
     * 切换标签显示
     */
    private void toggleLabels() {
        boolean showLabels = showLabelsToggle.isSelected();
        for (Label label : spotLabels.values()) {
            label.setVisible(showLabels);
        }
    }
    
    /**
     * 更新景点显示
     */
    private void updateSpotDisplay() {
        // 清除原有景点标记
        clearSpotMarkers();
        
        // 添加新的景点标记
        for (int i = 0; i < spotList.size(); i++) {
            ScenicSpot spot = spotList.get(i);
            addSpotMarker(spot);
        }
    }
    
    /**
     * 清除景点标记
     */
    private void clearSpotMarkers() {
        // 移除所有景点圆形
        for (Circle circle : spotCircles.values()) {
            mapPane.getChildren().remove(circle);
        }
        spotCircles.clear();
        
        // 移除所有景点标签
        for (Label label : spotLabels.values()) {
            mapPane.getChildren().remove(label);
        }
        spotLabels.clear();
    }
    
    /**
     * 添加景点标记
     * @param spot 景点
     */
    private void addSpotMarker(ScenicSpot spot) {
        // 创建景点圆形标记
        double x = transformX(spot.getX());
        double y = transformY(spot.getY());
        
        Circle circle = new Circle(x, y, 8);
        circle.setFill(getCategoryColor(spot.getCategory()));
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(2);
        
        // 设置景点信息提示
        boolean isEnglish = mainController.getUserPreference().isEnglish();
        Tooltip tooltip = new Tooltip(spot.getName(isEnglish));
        tooltip.setShowDelay(Duration.millis(100));
        Tooltip.install(circle, tooltip);
        
        // 添加点击事件
        circle.setOnMouseClicked(e -> {
            selectSpot(spot);
        });
        
        // 添加到地图
        mapPane.getChildren().add(circle);
        spotCircles.put(spot.getId(), circle);
        
        // 创建景点标签
        Label label = new Label(spot.getName(isEnglish));
        label.setLayoutX(x + 10);
        label.setLayoutY(y - 10);
        label.getStyleClass().add("spot-label");
        label.setVisible(showLabelsToggle.isSelected());
        
        // 添加到地图
        mapPane.getChildren().add(label);
        spotLabels.put(spot.getId(), label);
    }
    
    /**
     * 根据景点类别获取颜色
     * @param category 景点类别
     * @return 对应的颜色
     */
    private Color getCategoryColor(String category) {
        switch (category) {
            case "建筑":
                return Color.CRIMSON;
            case "祭坛":
                return Color.GOLD;
            case "石刻":
                return Color.SLATEGRAY;
            case "宫殿":
                return Color.ROYALBLUE;
            case "通道":
                return Color.SEAGREEN;
            case "景观":
                return Color.MEDIUMORCHID;
            case "亭子":
                return Color.ORANGERED;
            case "入口":
                return Color.BLACK;
            default:
                return Color.DODGERBLUE;
        }
    }
    
    /**
     * 坐标转换 - 经度到屏幕X坐标
     */
    private double transformX(double longitude) {
        // 简化实现，实际应用中应该使用地理坐标投影
        // 这里假设经度范围是116.403 - 116.413，对应地图宽度
        double minLon = 116.403;
        double maxLon = 116.413;
        return ((longitude - minLon) / (maxLon - minLon)) * mapImage.getWidth();
    }
    
    /**
     * 坐标转换 - 纬度到屏幕Y坐标
     */
    private double transformY(double latitude) {
        // 简化实现，实际应用中应该使用地理坐标投影
        // 这里假设纬度范围是39.873 - 39.885，对应地图高度
        double minLat = 39.873;
        double maxLat = 39.885;
        return ((maxLat - latitude) / (maxLat - minLat)) * mapImage.getHeight();
    }
    
    /**
     * 显示路径
     */
    private void showPaths() {
        // 清除现有路径
        hidePaths();
        
        // 基于最小生成树显示主要路径
        List<Edge> mstEdges = MST.kruskal(scenicGraph, Edge::getWeight);
        for (Edge edge : mstEdges) {
            ScenicSpot from = edge.getFrom().getSpot();
            ScenicSpot to = edge.getTo().getSpot();
            
            double x1 = transformX(from.getX());
            double y1 = transformY(from.getY());
            double x2 = transformX(to.getX());
            double y2 = transformY(to.getY());
            
            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(edge.isCrowded() ? Color.RED : Color.GRAY);
            line.setStrokeWidth(2);
            line.getStrokeDashArray().addAll(5.0, 5.0);
            
            // 添加到地图
            mapPane.getChildren().add(0, line); // 添加到底层
            pathLines.add(line);
        }
    }
    
    /**
     * 隐藏路径
     */
    private void hidePaths() {
        for (Line line : pathLines) {
            mapPane.getChildren().remove(line);
        }
        pathLines.clear();
    }
    
    /**
     * 选择景点
     * @param spot 景点
     */
    private void selectSpot(ScenicSpot spot) {
        selectedSpot = spot;
        mainController.selectSpot(spot);
        
        // 高亮显示选中的景点
        highlightSpot(spot);
        
        // 显示景点信息
        showSpotInfo(spot);
        
        // 显示到该景点的路径
        showPathToSelectedSpot();
    }
    
    /**
     * 高亮显示景点
     * @param spot 景点
     */
    public void highlightSpot(ScenicSpot spot) {
        // 重置所有景点样式
        for (Circle circle : spotCircles.values()) {
            circle.setRadius(8);
            circle.setEffect(null);
        }
        
        // 高亮选中的景点
        Circle selectedCircle = spotCircles.get(spot.getId());
        if (selectedCircle != null) {
            selectedCircle.setRadius(12);
            // 添加发光效果
            javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
            glow.setColor(Color.YELLOW);
            glow.setWidth(20);
            glow.setHeight(20);
            selectedCircle.setEffect(glow);
        }
    }
    
    /**
     * 显示景点信息
     * @param spot 景点
     */
    private void showSpotInfo(ScenicSpot spot) {
        boolean isEnglish = mainController.getUserPreference().isEnglish();
        
        spotNameLabel.setText(spot.getName(isEnglish));
        spotDescriptionText.setText(spot.getDescription(isEnglish));
        spotCategoryLabel.setText(LocaleUtil.getString("spot.category") + ": " + spot.getCategory());
        spotVisitTimeLabel.setText(LocaleUtil.getString("spot.visitTime") + ": " + 
                                 spot.getVisitTime() + " " + LocaleUtil.getString("time.minutes"));
        
        // 显示信息面板
        spotInfoBox.setVisible(true);
    }
    
    /**
     * 显示到选中景点的路径
     */
    private void showPathToSelectedSpot() {
        if (selectedSpot == null) return;
        
        // 清除现有路径
        hidePaths();
        
        // 使用默认的起点（东门）
        int startId = 9; // 东门ID
        
        // 定义权重函数
        Function<Edge, Double> weightFunction;
        if (avoidCrowdsCheckBox.isSelected()) {
            // 避开拥挤区域
            weightFunction = Edge::getEffectiveWeight;
        } else {
            // 使用默认权重
            weightFunction = Edge::getWeight;
        }
        
        // 计算最短路径
        Map<Integer, Integer> predecessor = ShortestPath.dijkstra(scenicGraph, startId, weightFunction);
        List<ScenicSpot> shortestPath = ShortestPath.constructPath(scenicGraph, predecessor, selectedSpot.getId());
        
        // 绘制路径
        if (shortestPath.size() > 1) {
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                ScenicSpot from = shortestPath.get(i);
                ScenicSpot to = shortestPath.get(i + 1);
                
                double x1 = transformX(from.getX());
                double y1 = transformY(from.getY());
                double x2 = transformX(to.getX());
                double y2 = transformY(to.getY());
                
                Line line = new Line(x1, y1, x2, y2);
                line.setStroke(Color.BLUE);
                line.setStrokeWidth(3);
                
                // 添加到地图
                mapPane.getChildren().add(line);
                pathLines.add(line);
            }
        }
    }
    
    /**
     * 将地图中心点设为指定景点
     * @param spot 景点
     */
    private void centerOnSpot(ScenicSpot spot) {
        double x = transformX(spot.getX()) * zoomLevel.get();
        double y = transformY(spot.getY()) * zoomLevel.get();
        
        double centerX = mapContainer.getWidth() / 2;
        double centerY = mapContainer.getHeight() / 2;
        
        mapPane.setLayoutX(centerX - x);
        mapPane.setLayoutY(centerY - y);
    }
}