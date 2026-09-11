package com.bilitro.model.hand;

import com.bilitro.model.card.Card;

/**
 * 计分上下文：在一次出牌计分过程中可被功能牌读写的中间状态。
 *
 * <p>对应计分流程图：
 * <ul>
 *   <li>初始值 = 牌型基础分 / 基础倍数（见 {@link HandType}）</li>
 *   <li>逐张手牌：chips += 手牌点数（{@code Rank.chips()}），随后依次触发功能牌</li>
 *   <li>功能牌通过 {@link #addChips} / {@link #addMult} / {@link #multiplyMult} 修改</li>
 *   <li>最终得分 = {@code chips × mult}（"得分 = 最终分数 × 最终倍数"）</li>
 * </ul>
 */
public final class ScoringContext {

    private int chips;   // 最终分数（加算）
    private int mult;    // 最终倍数
    private double multMultiplier = 1.0; // 倍数的乘算修正（如"倍数 ×2"类功能牌）

    /** 本次出牌的牌型（功能牌条件判断用）。 */
    private final HandType handType;
    /** 当前正在计分的手牌（花色条件功能牌用）。 */
    private Card currentCard;
    /** 是否正在处理第一张计分手牌（整手一次的效果只在这一步触发）。 */
    private boolean firstCard;
    /** 是否把所有手牌视为人头牌（功能牌"幻视"的元规则效果）。 */
    private boolean allFaceCards;
    /** 当前持有的功能牌数量（"每有一张功能牌"类效果用）。 */
    private int specialCount;
    /** 本次打出的全部手牌（"花盆"等按整手花色判定的效果用）。 */
    private java.util.List<Card> playedCards;
    /** 花色归并是否生效（"模糊小丑"：红桃=方块、梅花=黑桃）。 */
    private boolean mergeSuits;

    public ScoringContext(int baseChips, int baseMult) {
        this(baseChips, baseMult, null);
    }

    public ScoringContext(int baseChips, int baseMult, HandType handType) {
        this.chips = baseChips;
        this.mult = baseMult;
        this.handType = handType;
    }

    /** 由计分器在逐张计分时设置当前手牌与是否首张。 */
    public void setCurrentCard(Card card, boolean first) {
        this.currentCard = card;
        this.firstCard = first;
    }

    /** 返回本次出牌的牌型。 */
    public HandType handType() {
        return handType;
    }

    /** 返回当前正在计分的手牌。 */
    public Card currentCard() {
        return currentCard;
    }

    /** 返回当前是否第一张计分手牌。 */
    public boolean isFirstCard() {
        return firstCard;
    }

    /** 由计分器在计分前设置：是否把所有手牌视为人头牌。 */
    public void setAllFaceCards(boolean allFaceCards) {
        this.allFaceCards = allFaceCards;
    }

    /** 返回是否把所有手牌视为人头牌。 */
    public boolean allFaceCards() {
        return allFaceCards;
    }

    /** 由计分器在计分前设置：当前持有的功能牌数量。 */
    public void setSpecialCount(int specialCount) {
        this.specialCount = specialCount;
    }

    /** 返回当前持有的功能牌数量。 */
    public int specialCount() {
        return specialCount;
    }

    /** 由计分器在计分前设置：本次打出的全部手牌。 */
    public void setPlayedCards(java.util.List<Card> playedCards) {
        this.playedCards = playedCards;
    }

    /** 返回本次打出的全部手牌。 */
    public java.util.List<Card> playedCards() {
        return playedCards;
    }

    /** 由计分器在计分前设置：花色归并是否生效。 */
    public void setMergeSuits(boolean mergeSuits) {
        this.mergeSuits = mergeSuits;
    }

    /** 返回花色归并是否生效。 */
    public boolean mergeSuits() {
        return mergeSuits;
    }

    public int chips() {
        return chips;
    }

    public int mult() {
        return (int) Math.round(mult * multMultiplier);
    }

    /** 加分（手牌点数、功能牌加分都走这里）。 */
    public void addChips(int delta) {
        chips += delta;
    }

    /** 加倍数。 */
    public void addMult(int delta) {
        mult += delta;
    }

    /** 倍数乘算（如 ×2 类功能牌）。 */
    public void multiplyMult(double factor) {
        multMultiplier *= factor;
    }

    /** 最终得分 = 最终分数 × 最终倍数。 */
    public int finalScore() {
        return chips * mult();
    }
}
