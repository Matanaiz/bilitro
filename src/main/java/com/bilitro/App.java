package com.bilitro;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Bilitro 应用入口。
 * 职责：启动 JavaFX，装配 MVC，加载存档/主菜单。
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // TODO: 初始化 GameConfig / SaveManager / AudioManager
        // TODO: 检测自动存档，决定进入主菜单还是弹出"继续游戏"入口
        // TODO: 创建主场景并 show
        throw new UnsupportedOperationException("待实现");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
