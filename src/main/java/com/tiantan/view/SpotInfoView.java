package com.tiantan.view;

import com.tiantan.model.data.ScenicSpot;
import com.tiantan.util.LocaleUtil;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 景点详细信息视图类
 * 用于显示景点的详细信息对话框
 */
public class SpotInfoView {
    private static final Logger logger = LoggerFactory.getLogger(SpotInfoView.class);
    
    private Stage dialog;
    private ScenicSpot spot;
    private boolean isEnglish;
    
    /**
     * 构造函数
     * @param spot 景点
     * @param isEnglish 是否使用英文
     */
    public SpotInfoView(ScenicSpot spot, boolean isEnglish) {
        this.spot = spot;
        this.isEnglish = isEnglish;
        createDialog();
    }
    
    /**
     * 创建对话框
     */
    private void createDialog() {
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.DECORATED);
        dialog.setTitle(spot.getName(isEnglish));
        dialog.setMinWidth(500);
        dialog.setMinHeight(400);
        
        // 创建内容
        VBox content = createContent();
        
        // 创建对话框场景
        javafx.scene.Scene scene = new javafx.scene.Scene(content);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        dialog.setScene(scene);
    }
    
    /**
     * 创建对话框内容
     * @return 内容面板
     */
    private VBox createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("spot-info-dialog");
        
        // 景点名称
        Label nameLabel = new Label(spot.getName(isEnglish));
        nameLabel.getStyleClass().add("spot-name");
        
        // 景点图片
        ImageView imageView = null;
        try {
            Image image = new Image(getClass().getResourceAsStream(spot.getImageUrl()));
            imageView = new ImageView(image);
            imageView.setFitWidth(460);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            logger.warn("无法加载景点图片: " + spot.getImageUrl(), e);
        }
        
        // 景点信息
        Label categoryLabel = new Label(LocaleUtil.getString("spot.category") + ": " + spot.getCategory());
        Label visitTimeLabel = new Label(LocaleUtil.getString("spot.visitTime") + ": " + 
                                      spot.getVisitTime() + " " + LocaleUtil.getString("time.minutes"));
        
        // 景点描述
        TitledPane descriptionPane = new TitledPane();
        descriptionPane.setText(LocaleUtil.getString("spot.description"));
        descriptionPane.setExpanded(true);
        descriptionPane.setCollapsible(false);
        
        TextArea descriptionArea = new TextArea(spot.getDescription(isEnglish));
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefHeight(150);
        descriptionArea.setEditable(false);
        descriptionPane.setContent(descriptionArea);
        
        // 可访问性信息
        Label accessibleLabel = new Label(spot.isAccessible() ? 
                                        "♿ " + LocaleUtil.getString("spot.accessible") : 
                                        "⚠ " + LocaleUtil.getString("spot.notAccessible"));
        
        // 按钮区域
        Button closeButton = new Button(LocaleUtil.getString("dialog.close"));
        closeButton.setOnAction(e -> dialog.close());
        
        // 构建布局
        HBox buttonBar = new HBox(10);
        buttonBar.getChildren().add(closeButton);
        buttonBar.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        // 添加到内容面板
        content.getChildren().addAll(nameLabel);
        if (imageView != null) {
            content.getChildren().add(imageView);
        }
        content.getChildren().addAll(categoryLabel, visitTimeLabel, accessibleLabel, descriptionPane, buttonBar);
        
        return content;
    }
    
    /**
     * 显示对话框
     */
    public void show() {
        dialog.showAndWait();
    }
}