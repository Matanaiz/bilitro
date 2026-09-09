package com.bilitro.model.hand;

/**
 * 牌型枚举（需求 5.1 假设 1，牌型表已定稿）。
 *
 * <p>优先级、初始分数、初始倍数对照表：
 * <pre>
 *   牌型     优先级  初始分数  初始倍数
 *   同花顺     9      100      ×8
 *   四条       8       60      ×7
 *   葫芦       7       40      ×4
 *   同花       6       35      ×4
 *   顺子       5       30      ×4
 *   三条       4       30      ×3
 *   两对       3       20      ×2
 *   对子       2       10      ×2
 *   高牌       1        5      ×1
 * </pre>
 * 得分 = 初始分数 × 初始倍数（功能牌加成由 ScoreCalculator 叠加）。
 */
public enum HandType implements com.bilitro.model.game.GameSession.HandTypeView {
    HIGH_CARD(1, 5, 1),          // 高牌
    PAIR(2, 10, 2),              // 对子
    TWO_PAIR(3, 20, 2),          // 两对
    THREE_OF_A_KIND(4, 30, 3),   // 三条
    STRAIGHT(5, 30, 4),          // 顺子
    FLUSH(6, 35, 4),             // 同花
    FULL_HOUSE(7, 40, 4),        // 葫芦
    FOUR_OF_A_KIND(8, 60, 7),    // 四条
    STRAIGHT_FLUSH(9, 100, 8);   // 同花顺

    private final int priority;
    private final int baseScore;
    private final int baseMultiplier;

    HandType(int priority, int baseScore, int baseMultiplier) {
        this.priority = priority;
        this.baseScore = baseScore;
        this.baseMultiplier = baseMultiplier;
    }

    /** 优先级（数值越大牌型越高）。 */
    public int priority() {
        return priority;
    }

    /** 牌型初始分数。 */
    public int baseScore() {
        return baseScore;
    }

    /** 牌型初始倍数。 */
    public int baseMultiplier() {
        return baseMultiplier;
    }

    /** 不含功能牌加成的基础得分 = 初始分数 × 初始倍数。 */
    public int baseTotal() {
        return baseScore * baseMultiplier;
    }

    /** 中文显示名（供 view 层展示，实现 GameSession.HandTypeView）。 */
    @Override
    public String displayName() {
        return switch (this) {
            case HIGH_CARD -> "高牌";
            case PAIR -> "对子";
            case TWO_PAIR -> "两对";
            case THREE_OF_A_KIND -> "三条";
            case STRAIGHT -> "顺子";
            case FLUSH -> "同花";
            case FULL_HOUSE -> "葫芦";
            case FOUR_OF_A_KIND -> "四条";
            case STRAIGHT_FLUSH -> "同花顺";
        };
    }
}
