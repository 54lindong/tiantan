package com.tiantan.view;

import com.tiantan.controller.MainController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * 主界面视图类
 * 负责加载和初始化主界面
 */
public class MainView {
    private static final Logger logger = LoggerFactory.getLogger(MainView.class);
    
    private Stage primaryStage;
    private MainController controller;
    private ResourceBundle resources;
    
    /**
     * 构造函数
     * @param primaryStage 主舞台
     * @param resources 国际化资源
     */
    public MainView(Stage primaryStage, ResourceBundle resources) {
        this.primaryStage = primaryStage;
        this.resources = resources;
    }
    
    /**
     * 初始化视图
     * @return 初始化是否成功
     */
    public boolean initialize() {
        try {
            // 加载FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"), resources);
            Parent root = loader.load();
            
            // 获取控制器引用
            controller = loader.getController();
            
            // 配置场景
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // 配置舞台
            primaryStage.setTitle(resources.getString("app.title"));
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            logger.info("主视图初始化成功");
            return true;
        } catch (IOException e) {
            logger.error("主视图初始化失败", e);
            return false;
        }
    }
    
    /**
     * 显示视图
     */
    public void show() {
        primaryStage.show();
    }
    
    /**
     * 获取控制器
     * @return 主控制器
     */
    public MainController getController() {
        return controller;
    }
}