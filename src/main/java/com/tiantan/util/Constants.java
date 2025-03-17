package com.tiantan.util;

/**
 * 应用程序常量定义
 */
public class Constants {
    // 应用程序信息
    public static final String APP_NAME = "TianTanGuide";
    public static final String APP_VERSION = "1.0.0";
    
    // 文件路径
    public static final String DATA_DIR = "data";
    public static final String SPOTS_FILE = "spots.json";
    public static final String GRAPH_FILE = "graph.csv";
    public static final String SETTINGS_FILE = "settings.properties";
    public static final String ROUTES_FILE = "routes.json";
    
    // 地图相关常量
    public static final double MAP_DEFAULT_ZOOM = 1.0;
    public static final double MAP_MIN_ZOOM = 0.5;
    public static final double MAP_MAX_ZOOM = 3.0;
    public static final double MAP_ZOOM_STEP = 0.1;
    
    // 景点分类
    public static final String[] SPOT_CATEGORIES = {
        "建筑", "祭坛", "石刻", "宫殿", "通道", "景观", "亭子", "入口"
    };
    
    // 搜索相关
    public static final int SEARCH_MAX_RESULTS = 20;
    public static final int SEARCH_FUZZY_THRESHOLD = 2; // 最大编辑距离
    
    // 路线规划
    public static final int MAX_ROUTE_STOPS = 15;
    public static final int DEFAULT_VISIT_TIME = 30; // 默认游览时间（分钟）
    
    // 界面相关
    public static final String[] AVAILABLE_THEMES = {
        "light", "dark", "blue"
    };
    
    public static final String[] FONT_SIZES = {
        "small", "medium", "large"
    };
    
    // 国际化资源键前缀
    public static final String KEY_SPOT = "spot.";
    public static final String KEY_ROUTE = "route.";
    public static final String KEY_UI = "ui.";
    public static final String KEY_ERROR = "error.";
    
    // 错误消息
    public static final String ERROR_DATA_LOAD = "error.data.load";
    public static final String ERROR_DATA_SAVE = "error.data.save";
    public static final String ERROR_PATH_NOT_FOUND = "error.path.notFound";
    public static final String ERROR_INVALID_SPOT = "error.spot.invalid";
    
    // 国际化资源键
    public static final String KEY_APP_TITLE = "app.title";
    public static final String KEY_MAP_TAB = "ui.tab.map";
    public static final String KEY_SEARCH_TAB = "ui.tab.search";
    public static final String KEY_ROUTE_TAB = "ui.tab.route";
    public static final String KEY_SETTINGS_TAB = "ui.tab.settings";
    
    // 设置键
    public static final String SETTING_LANGUAGE = "language";
    public static final String SETTING_THEME = "theme";
    public static final String SETTING_FONT_SIZE = "fontSize";
    public static final String SETTING_MAP_ZOOM = "defaultMapZoom";
    public static final String SETTING_CROWD_WARNING = "showCrowdWarning";
    public static final String SETTING_AUTO_SAVE = "autoSaveInterval";
    
    // 未找到资源时的替代文本
    public static final String FALLBACK_TEXT = "[资源未找到]";
    
    // 私有构造函数，防止实例化
    private Constants() {
        throw new IllegalStateException("常量类不应被实例化");
    }
}