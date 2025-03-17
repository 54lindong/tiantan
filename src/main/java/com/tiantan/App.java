package com.tiantan;

import com.tiantan.util.LocaleUtil;
import com.tiantan.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 天坛导游应用主类
 */
public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // 设置默认语言环境
            Locale.setDefault(LocaleUtil.getDefaultLocale());
            
            // 加载国际化资源
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
            
            // 使用MainView类初始化主界面
            MainView mainView = new MainView(primaryStage, bundle);
            if (mainView.initialize()) {
                mainView.show();
                logger.info("应用启动成功");
            } else {
                logger.error("应用初始化失败");
            }
        } catch (Exception e) {
            logger.error("应用启动失败", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

/**
 * 启动器类 - 用于创建可执行JAR
 */
class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}