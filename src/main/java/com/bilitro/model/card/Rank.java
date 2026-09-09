package com.bilitro.model.card;

/** 点数。 */
public enum Rank {
    TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN,
    JACK, QUEEN, KING, ACE;

    /** 用于牌型比较的大小值，具体映射待确认（A 大还是 2 大等）。 */
    public int value() {
        // TODO: 待确认点数大小规则
        throw new UnsupportedOperationException("待实现");
    }
}
