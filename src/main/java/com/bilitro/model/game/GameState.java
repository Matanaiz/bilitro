package com.bilitro.model.game;

/** 一局游戏的整体状态机。 */
public enum GameState {
    MAIN_MENU,
    IN_ROUND,      // 回合内（选牌/出牌/弃牌）
    LEVEL_CLEAR,   // 过关，进商店前
    SHOPPING,
    GAME_OVER,     // 失败结算
    VICTORY        // 通关结算
}
