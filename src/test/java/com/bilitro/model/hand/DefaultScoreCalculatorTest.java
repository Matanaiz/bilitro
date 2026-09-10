package com.bilitro.model.hand;

import com.bilitro.model.card.Card;
import com.bilitro.model.card.Rank;
import com.bilitro.model.card.Suit;
import com.bilitro.model.player.ConfiguredSpecialCard;
import com.bilitro.model.player.SpecialCard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 计分器测试：基础公式与功能牌触发轮次。 */
class DefaultScoreCalculatorTest {

    private final ScoreCalculator calculator = new DefaultScoreCalculator();

    /** 构造计分输入的简写。 */
    private static Evaluation eval(HandType type, Card... cards) {
        return new Evaluation(type, List.of(cards));
    }

    private static Card c(Rank rank) {
        return new Card(Suit.SPADE, rank);
    }

    /** 构造一张加分类功能牌。 */
    private static SpecialCard addChips(int value) {
        return new ConfiguredSpecialCard("t", "t", "t", 1, "",
                ConfiguredSpecialCard.EffectType.ADD_CHIPS, value);
    }

    @Test
    void 无功能牌时得分等于基础分加点数乘倍数() {
        // 对子 10×2，牌面 10 + 10：得分 = (10 + 20) × 2 = 60
        var result = calculator.score(
                eval(HandType.PAIR, c(Rank.TEN), c(Rank.TEN)), List.of());
        assertEquals(10, result.baseChips());
        assertEquals(20, result.cardChips());
        assertEquals(0, result.bonusChips());
        assertEquals(2, result.baseMult());
        assertEquals(60, result.finalScore());
    }

    @Test
    void 人头牌计10分A计11分() {
        // 对子 K + A 的点数部分：10 + 11 = 21
        var result = calculator.score(
                eval(HandType.PAIR, c(Rank.KING), c(Rank.ACE)), List.of());
        assertEquals(21, result.cardChips());
    }

    @Test
    void 功能牌每张手牌触发一轮() {
        // 对子计 2 张牌，+30 分功能牌触发 2 轮：加成 60
        var result = calculator.score(
                eval(HandType.PAIR, c(Rank.TEN), c(Rank.TEN)),
                List.of(addChips(30)));
        assertEquals(60, result.bonusChips());
        // (10 + 20 + 60) × 2 = 180
        assertEquals(180, result.finalScore());
    }

    @Test
    void 高牌五张只计最大一张() {
        // 高牌 5×1，只有 A 计分：(5 + 11) × 1 = 16
        var result = calculator.score(
                eval(HandType.HIGH_CARD, c(Rank.ACE)), List.of());
        assertEquals(16, result.finalScore());
    }
}
