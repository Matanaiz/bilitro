package com.bilitro.model.card;

import java.util.List;

/**
 * 牌组（抽牌堆）。
 * 对应需求 2.1.3 随机抽牌、2.2.6 剩余牌组查看。
 */
public interface Deck {

    /** 随机抽出 n 张牌；不足 n 张时全部抽出。 */
    List<Card> draw(int n);

    /** 剩余牌数 R。 */
    int remaining();

    /** 剩余牌的只读快照，供"查看牌组"界面按花色分组展示。 */
    List<Card> peekRemaining();

    /** 重置为完整牌组（可排除被禁用花色，见 LevelRule）。 */
    void reset(java.util.Set<Suit> bannedSuits);
}
