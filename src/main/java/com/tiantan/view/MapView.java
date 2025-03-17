package com.tiantan.view;

import com.tiantan.controller.MapController;
import com.tiantan.controller.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * 地图视图类
 * 负责加载和初始化地图界面
 */
public class MapView {
    private static final Logger logger = LoggerFactory.getLogger(MapView.class);
    
    private Parent view;
    private MapController controller;
    private MainController mainController;
    private ResourceBundle resources;
    
    /**
     * 构造函数
     * @param mainController 主控制器
     * @param resources 国际化资源
     */
    public MapView(MainController mainController, ResourceBundle resources) {
        this.mainController = mainController;
        this.resources = resources;
    }
    
    /**
     * 初始化视图
     * @return 初始化是否成功
     */
    public boolean initialize() {
        try {
            // 加载FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MapView.fxml"), resources);
            view = loader.load();
            
            // 获取控制器引用
            controller = loader.getController();
            
            // 设置主控制器
            controller.setMainController(mainController);
            
            logger.info("地图视图初始化成功");
            return true;
        } catch (IOException e) {
            logger.error("地图视图初始化失败", e);
            return false;
        }
    }
    
    /**
     * 获取视图
     * @return 视图节点
     */
    public Parent getView() {
        return view;
    }
    
    /**
     * 获取控制器
     * @return 地图控制器
     */
    public MapController getController() {
        return controller;
    }
}