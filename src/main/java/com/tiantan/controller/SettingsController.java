package com.tiantan.controller;

import com.tiantan.util.Constants;
import com.tiantan.util.FileUtil;
import com.tiantan.util.LocaleUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * 设置控制器
 */
public class SettingsController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);
    
    @FXML private BorderPane mainPane;
    @FXML private ToggleGroup languageGroup;
    @FXML private RadioButton chineseRadio;
    @FXML private RadioButton englishRadio;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private ComboBox<String> fontSizeComboBox;
    @FXML private Slider mapZoomSlider;
    @FXML private CheckBox showCrowdWarningCheckBox;
    @FXML private Spinner<Integer> autoSaveIntervalSpinner;
    @FXML private Button saveSettingsButton;
    @FXML private Button resetSettingsButton;
    @FXML private Label versionLabel;
    
    private MainController mainController;
    private Properties settings;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化控件
        initControls();
        
        // 设置事件处理
        initEventHandlers();
    }
    
    /**
     * 设置主控制器
     * @param controller 主控制器
     */
    public void setMainController(MainController controller) {
        this.mainController = controller;
        this.settings = mainController.getSettings();
        
        // 加载设置
        loadSettings();
    }
    
    /**
     * 初始化控件
     */
    private void initControls() {
        // 初始化主题下拉框
        themeComboBox.getItems().addAll(Constants.AVAILABLE_THEMES);
        
        // 初始化字体大小下拉框
        fontSizeComboBox.getItems().addAll(Constants.FONT_SIZES);
        
        // 初始化地图缩放滑块
        mapZoomSlider.setMin(Constants.MAP_MIN_ZOOM);
        mapZoomSlider.setMax(Constants.MAP_MAX_ZOOM);
        mapZoomSlider.setValue(Constants.MAP_DEFAULT_ZOOM);
        
        // 初始化自动保存间隔选择器
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(60, 1800, 300, 60);
        autoSaveIntervalSpinner.setValueFactory(valueFactory);
        
        // 设置版本标签
        versionLabel.setText(Constants.APP_NAME + " v" + Constants.APP_VERSION);
    }
    
    /**
     * 初始化事件处理
     */
    private void initEventHandlers() {
        // 保存设置按钮
        saveSettingsButton.setOnAction(e -> saveSettings());
        
        // 重置设置按钮
        resetSettingsButton.setOnAction(e -> resetSettings());
        
        // 语言切换
        chineseRadio.setOnAction(e -> {
            if (chineseRadio.isSelected()) {
                mainController.switchLanguage(false);
            }
        });
        
        englishRadio.setOnAction(e -> {
            if (englishRadio.isSelected()) {
                mainController.switchLanguage(true);
            }
        });
    }
    
    /**
     * 加载设置
     */
    public void loadSettings() {
        // 语言设置
        String language = settings.getProperty(Constants.SETTING_LANGUAGE, "zh");
        if ("en".equals(language)) {
            englishRadio.setSelected(true);
        } else {
            chineseRadio.setSelected(true);
        }
        
        // 主题设置
        String theme = settings.getProperty(Constants.SETTING_THEME, "light");
        themeComboBox.getSelectionModel().select(theme);
        
        // 字体大小设置
        String fontSize = settings.getProperty(Constants.SETTING_FONT_SIZE, "medium");
        fontSizeComboBox.getSelectionModel().select(fontSize);
        
        // 地图缩放设置
        double mapZoom = Double.parseDouble(settings.getProperty(Constants.SETTING_MAP_ZOOM, "1.0"));
        mapZoomSlider.setValue(mapZoom);
        
        // 拥挤警告设置
        boolean showCrowdWarning = Boolean.parseBoolean(
            settings.getProperty(Constants.SETTING_CROWD_WARNING, "true"));
        showCrowdWarningCheckBox.setSelected(showCrowdWarning);
        
        // 自动保存间隔设置
        int autoSaveInterval = Integer.parseInt(
            settings.getProperty(Constants.SETTING_AUTO_SAVE, "300"));
        autoSaveIntervalSpinner.getValueFactory().setValue(autoSaveInterval);
    }
    
    /**
     * 保存设置
     */
    @FXML
    private void saveSettings() {
        // 语言设置
        settings.setProperty(Constants.SETTING_LANGUAGE, englishRadio.isSelected() ? "en" : "zh");
        
        // 主题设置
        settings.setProperty(Constants.SETTING_THEME, themeComboBox.getValue());
        
        // 字体大小设置
        settings.setProperty(Constants.SETTING_FONT_SIZE, fontSizeComboBox.getValue());
        
        // 地图缩放设置
        settings.setProperty(Constants.SETTING_MAP_ZOOM, Double.toString(mapZoomSlider.getValue()));
        
        // 拥挤警告设置
        settings.setProperty(Constants.SETTING_CROWD_WARNING, 
                           Boolean.toString(showCrowdWarningCheckBox.isSelected()));
        
        // 自动保存间隔设置
        settings.setProperty(Constants.SETTING_AUTO_SAVE, 
                           Integer.toString(autoSaveIntervalSpinner.getValue()));
        
        // 保存到文件
        if (FileUtil.saveSettings(settings)) {
            mainController.showStatus(LocaleUtil.getString("status.settingsSaved"));
            
            // 应用主题变化
            applyTheme(themeComboBox.getValue());
            
            // 应用字体大小变化
            applyFontSize(fontSizeComboBox.getValue());
        } else {
            mainController.showStatus(LocaleUtil.getString(Constants.ERROR_DATA_SAVE));
        }
    }
    
    /**
     * 重置设置
     */
    @FXML
    private void resetSettings() {
        // 确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LocaleUtil.getString("settings.reset.title"));
        alert.setHeaderText(null);
        alert.setContentText(LocaleUtil.getString("settings.reset.confirm"));
        
        // 显示对话框并等待用户响应
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 重置为默认设置
                chineseRadio.setSelected(true);
                themeComboBox.getSelectionModel().select("light");
                fontSizeComboBox.getSelectionModel().select("medium");
                mapZoomSlider.setValue(Constants.MAP_DEFAULT_ZOOM);
                showCrowdWarningCheckBox.setSelected(true);
                autoSaveIntervalSpinner.getValueFactory().setValue(300);
                
                // 保存设置
                saveSettings();
                
                // 切换回中文
                mainController.switchLanguage(false);
            }
        });
    }
    
    /**
     * 应用主题
     * @param theme 主题名称
     */
    private void applyTheme(String theme) {
        Scene scene = null;
        
        // 获取当前窗口的场景
        if (mainPane != null && mainPane.getScene() != null) {
            scene = mainPane.getScene();
        }
        
        if (scene != null) {
            // 清除旧主题
            scene.getStylesheets().removeAll(
                getClass().getResource("/css/themes/light.css").toExternalForm(),
                getClass().getResource("/css/themes/dark.css").toExternalForm(),
                getClass().getResource("/css/themes/blue.css").toExternalForm()
            );
            
            // 应用新主题
            String themeUrl = "/css/themes/" + theme + ".css";
            scene.getStylesheets().add(
                getClass().getResource(themeUrl).toExternalForm()
            );
        } else {
            logger.error("无法应用主题，场景未初始化");
        }
    }

    /**
     * 应用字体大小
     * @param fontSize 字体大小
     */
    private void applyFontSize(String fontSize) {
        Scene scene = null;
        
        // 获取当前窗口的场景
        if (mainPane != null && mainPane.getScene() != null) {
            scene = mainPane.getScene();
        }
        
        if (scene != null) {
            // 清除旧字体大小样式
            scene.getStylesheets().removeAll(
                getClass().getResource("/css/font-sizes/small.css").toExternalForm(),
                getClass().getResource("/css/font-sizes/medium.css").toExternalForm(),
                getClass().getResource("/css/font-sizes/large.css").toExternalForm()
            );
            
            // 应用新字体大小
            String fontSizeUrl = "/css/font-sizes/" + fontSize + ".css";
            scene.getStylesheets().add(
                getClass().getResource(fontSizeUrl).toExternalForm()
            );
        } else {
            logger.error("无法应用字体大小，场景未初始化");
        }
    }
}