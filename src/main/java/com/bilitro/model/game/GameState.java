package com.bilitro.model.game;

/**
 * 一局游戏的整体状态机。
 *
 * <p>状态迁移：
 * <pre>
 *   MAIN_MENU → IN_ROUND（新一局 / 继续游戏）
 *   IN_ROUND ⇄ PAUSED（暂停 / 继续；暂停界面提供设置与回主菜单入口）
 *   IN_ROUND → LEVEL_CLEAR（达标且非第 8 关）→ SHOPPING → IN_ROUND（下一关）
 *   IN_ROUND → VICTORY（第 8 关达标）
 *   IN_ROUND → FAILED（出牌次数耗尽且未达标）
 *   PAUSED → MAIN_MENU（离开本局，先触发自动存档）
 *   VICTORY / FAILED → MAIN_MENU 或再来一局
 * </pre>
 * 查看牌组、功能牌说明等为视图层浮层，不属于游戏状态。
 */
public enum GameState {
    MAIN_MENU,     // 主菜单
    IN_ROUND,      // 回合内（选牌/出牌/弃牌）
    PAUSED,        // 暂停（半透明菜单：继续/设置/回主菜单）
    LEVEL_CLEAR,   // 过关，进商店前
    SHOPPING,      // 商店
    FAILED,        // 失败结算
    VICTORY        // 通关结算
}
