package com.tiantan.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 国际化工具类
 */
public class LocaleUtil {
    private static final String BUNDLE_BASE_NAME = "i18n.messages";
    private static Locale currentLocale = Locale.CHINESE;
    private static ResourceBundle resourceBundle;
    
    static {
        // 初始化资源包
        loadResourceBundle();
    }
    
    /**
     * 获取默认语言环境
     * @return 默认Locale
     */
    public static Locale getDefaultLocale() {
        return Locale.CHINESE;
    }
    
    /**
     * 获取当前语言环境
     * @return 当前Locale
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }
    
    /**
     * 切换到英文
     */
    public static void switchToEnglish() {
        setLocale(Locale.ENGLISH);
    }
    
    /**
     * 切换到中文
     */
    public static void switchToChinese() {
        setLocale(Locale.CHINESE);
    }
    
    /**
     * 设置语言环境
     * @param locale 新的Locale
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        loadResourceBundle();
    }
    
    /**
     * 加载资源包
     */
    private static void loadResourceBundle() {
        resourceBundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, currentLocale);
    }
    
    /**
     * 获取国际化文本
     * @param key 资源键
     * @return 对应当前语言的文本
     */
    public static String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            return key; // 如果找不到对应的key，返回key本身
        }
    }
    
    /**
     * 获取带参数的国际化文本
     * @param key 资源键
     * @param args 替换参数
     * @return 格式化后的文本
     */
    public static String getString(String key, Object... args) {
        try {
            String pattern = resourceBundle.getString(key);
            return String.format(currentLocale, pattern, args);
        } catch (Exception e) {
            return key; // 如果出错，返回key本身
        }
    }
    
    /**
     * 检查当前是否为英文环境
     * @return 如果是英文环境返回true
     */
    public static boolean isEnglish() {
        return Locale.ENGLISH.getLanguage().equals(currentLocale.getLanguage());
    }
}