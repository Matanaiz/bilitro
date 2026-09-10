package com.bilitro.model;

/**
 * 全局游戏数值配置（数值已按小组答复定稿，见《项目实现思路.md》第四节）。
 * 注意：手牌数、出牌/弃牌次数均可被关卡规则或功能牌修改，这里只是初始值。
 */
public final class GameConfig {

    public static final int HAND_SIZE = 8;            // 初始手牌数（功能牌可修改）
    public static final int MIN_SELECT = 1;
    public static final int MAX_SELECT = 5;
    public static final int PLAYS_PER_LEVEL = 4;      // 初始出牌次数（关卡/功能牌可修改）
    public static final int DISCARDS_PER_LEVEL = 4;   // 初始弃牌次数（关卡/功能牌可修改）
    public static final int SPECIAL_CARD_LIMIT = 6;
    public static final int INITIAL_COINS = 0;

    /** 总关卡数：打到第 8 关并达标即本局胜利（见结束判定流程图）。 */
    public static final int MAX_LEVEL = 8;

    /** 每关通关固定奖励代币。 */
    public static final int LEVEL_CLEAR_REWARD = 4;

    /** 每剩余 1 次出牌次数额外奖励代币。 */
    public static final int COIN_PER_REMAINING_PLAY = 1;

    /** 利息：上回合每剩余 N 代币获得 1 代币。 */
    public static final int INTEREST_EVERY_N_COINS = 5;

    /** 第一关目标分；之后每关 ×2。 */
    public static final int LEVEL1_TARGET_SCORE = 200;
    public static final double TARGET_SCORE_GROWTH = 2.0;

    /** 商店刷新费用：初始 2 代币，每刷新一次 +3，每次进商店重置。 */
    public static final int SHOP_REFRESH_BASE_COST = 2;
    public static final int SHOP_REFRESH_COST_STEP = 3;

    private GameConfig() {
    }
}
