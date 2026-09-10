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
                ConfiguredSpecialCard.EffectType.ADD_CHIPS, value,
                ConfiguredSpecialCard.ConditionType.ALWAYS, null);
    }

    /** 构造一张配置表小丑牌。 */
    private static SpecialCard joker(String id, ConfiguredSpecialCard.EffectType effect,
                                     double value, ConfiguredSpecialCard.ConditionType cond,
                                     String condValue) {
        return new ConfiguredSpecialCard(id, id, id, 1, "", effect, value, cond, condValue);
    }

    @Test
    void 小丑结算时只加一次倍率() {
        // 对子 10×2，牌面 10+10；小丑无条件 +4 倍率整手一次：2+4=6
        var result = calculator.score(
                eval(HandType.PAIR, c(Rank.TEN), c(Rank.TEN)),
                List.of(joker("joker", ConfiguredSpecialCard.EffectType.ADD_MULT, 4,
                        ConfiguredSpecialCard.ConditionType.ALWAYS, null)));
        assertEquals(6, result.finalMult());
        // (10 + 20) × 6 = 180
        assertEquals(180, result.finalScore());
    }

    @Test
    void 色欲小丑只有红桃牌触发() {
        // 对子：红桃10 + 黑桃10；色欲小丑只给红桃 +3：倍数 2+3=5
        var result = calculator.score(
                eval(HandType.PAIR,
                        new Card(Suit.HEART, Rank.TEN), new Card(Suit.SPADE, Rank.TEN)),
                List.of(joker("lusty_joker", ConfiguredSpecialCard.EffectType.ADD_MULT, 3,
                        ConfiguredSpecialCard.ConditionType.SCORING_SUIT, "HEART")));
        assertEquals(5, result.finalMult());
    }

    @Test
    void 奸诈小丑整手只触发一次() {
        // 对子 +50 积分，整手一次（不是每张 +50）
        var result = calculator.score(
                eval(HandType.PAIR, c(Rank.TEN), c(Rank.TEN)),
                List.of(joker("sly_joker", ConfiguredSpecialCard.EffectType.ADD_CHIPS, 50,
                        ConfiguredSpecialCard.ConditionType.HAND_TYPE, "PAIR")));
        assertEquals(50, result.bonusChips());
        // (10 + 20 + 50) × 2 = 160
        assertEquals(160, result.finalScore());
    }

    @Test
    void 奸诈小丑对不含对子的牌型不触发() {
        var result = calculator.score(
                eval(HandType.HIGH_CARD, c(Rank.ACE)),
                List.of(joker("sly_joker", ConfiguredSpecialCard.EffectType.ADD_CHIPS, 50,
                        ConfiguredSpecialCard.ConditionType.HAND_TYPE, "PAIR")));
        assertEquals(0, result.bonusChips());
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
    void 无条件功能牌结算时只触发一次() {
        // 对子计 2 张牌，无条件 +30 分功能牌整手只触发一次：加成 30
        var result = calculator.score(
                eval(HandType.PAIR, c(Rank.TEN), c(Rank.TEN)),
                List.of(addChips(30)));
        assertEquals(30, result.bonusChips());
        // (10 + 20 + 30) × 2 = 120
        assertEquals(120, result.finalScore());
    }

    @Test
    void 高牌五张只计最大一张() {
        // 高牌 5×1，只有 A 计分：(5 + 11) × 1 = 16
        var result = calculator.score(
                eval(HandType.HIGH_CARD, c(Rank.ACE)), List.of());
        assertEquals(16, result.finalScore());
    }
}
