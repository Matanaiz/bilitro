package com.bilitro.model;

/**
 * 全局游戏数值配置（待定项集中在 TODO 注释中）。
 * TODO: 以下数值均为需求文档中的"示例"，需小组确认后定稿：
 *  - 手牌上限（示例 5 张）
 *  - 出牌选中张数范围（示例 1~5）
 *  - 每关初始出牌/弃牌次数
 *  - 功能牌持有上限（示例 5）
 *  - 初始货币、通关奖励货币
 */
public final class GameConfig {

    public static final int HAND_SIZE = 8;
    public static final int MIN_SELECT = 1;
    public static final int MAX_SELECT = 5;
    public static final int PLAYS_PER_LEVEL = 4;
    public static final int DISCARDS_PER_LEVEL = 4;
    public static final int SPECIAL_CARD_LIMIT = 6;
    public static final int INITIAL_COINS = 0;

    private GameConfig() {
    }
}
