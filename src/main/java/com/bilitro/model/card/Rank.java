package com.bilitro.model.card;

/**
 * 点数。
 * 大小规则已定稿：A 最大；顺子不允许 A-2-3-4-5（见《项目实现思路.md》答复 7）。
 */
public enum Rank {
    TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
    JACK(11), QUEEN(12), KING(13), ACE(14);

    private final int value;

    Rank(int value) {
        this.value = value;
    }

    /** 用于牌型比较的大小值（A=14 最大）。 */
    public int value() {
        return value;
    }

    /**
     * 计分点数（计分流程图中"分数 += 手牌点数"）：
     * 2~10 按面值，J/Q/K 计 10，A 计 11。
     */
    public int chips() {
        return switch (this) {
            case JACK, QUEEN, KING -> 10;
            case ACE -> 11;
            default -> value;
        };
    }
}
