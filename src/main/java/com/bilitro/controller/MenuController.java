package com.bilitro.controller;

/**
 * 主菜单 / 结算界面控制器。
 * 对应"用户做了什么"：新一局、继续游戏、再来一局、设置、退出。
 */
public interface MenuController {

    /** 开始新一局（清除旧存档，从第 1 关重新开局）。 */
    void onNewGame();

    /** 检测到存档时的"继续游戏"入口（需求 2.2.7）；无存档时无响应。 */
    void onContinueGame();

    /** 结算界面"再来一局"：胜利/失败后直接重开新一局。 */
    void onRestart();

    /** 打开设置界面（音效/BGM 开关等，P2）。 */
    void onOpenSettings();

    /** 退出游戏。 */
    void onExit();

    /** 启动时是否存在可用存档，决定主菜单是否点亮"继续游戏"按钮。 */
    boolean hasSave();
}
