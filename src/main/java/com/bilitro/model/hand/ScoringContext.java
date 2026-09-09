package com.bilitro.model.hand;

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

    public ScoringContext(int baseChips, int baseMult) {
        this.chips = baseChips;
        this.mult = baseMult;
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
