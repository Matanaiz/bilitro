package com.bilitro.model.player;

import com.bilitro.model.card.Card;
import com.bilitro.model.card.Rank;
import com.bilitro.model.card.Suit;
import com.bilitro.model.hand.Evaluation;
import com.bilitro.model.hand.HandType;
import com.bilitro.model.hand.DefaultScoreCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 功能牌配置驱动类测试：花色归并、牌型包含、乘算倍率、随机花色、描述文本。 */
class ConfiguredSpecialCardTest {

    private final DefaultScoreCalculator calculator = new DefaultScoreCalculator();

    /** 构造一张配置表功能牌的简写。 */
    private static ConfiguredSpecialCard joker(String id, ConfiguredSpecialCard.EffectType effect,
                                               double value, ConfiguredSpecialCard.ConditionType cond,
                                               String condValue) {
        return new ConfiguredSpecialCard(id, id, id, 1, "", effect, value, cond, condValue);
    }

    private static Card c(Suit suit, Rank rank) {
        return new Card(suit, rank);
    }

    @Test
    void 模糊小丑让方块牌触发红桃条件() {
        // 色欲小丑（红桃+3）+ 模糊小丑：方块 10 也按红桃触发，两张都 +3，倍率 2+6=8
        var result = calculator.score(
                new Evaluation(HandType.PAIR,
                        List.of(c(Suit.HEART, Rank.TEN), c(Suit.DIAMOND, Rank.TEN))),
                List.of(joker("lusty_joker", ConfiguredSpecialCard.EffectType.ADD_MULT, 3,
                                ConfiguredSpecialCard.ConditionType.SCORING_SUIT, "HEART"),
                        joker("smeared_joker", ConfiguredSpecialCard.EffectType.ADD_CHIPS, 0,
                                ConfiguredSpecialCard.ConditionType.META_MERGE_SUITS, null)));
        assertEquals(8, result.finalMult());
    }

    @Test
    void 无模糊小丑时方块牌不触发红桃条件() {
        var result = calculator.score(
                new Evaluation(HandType.PAIR,
                        List.of(c(Suit.HEART, Rank.TEN), c(Suit.DIAMOND, Rank.TEN))),
                List.of(joker("lusty_joker", ConfiguredSpecialCard.EffectType.ADD_MULT, 3,
                        ConfiguredSpecialCard.ConditionType.SCORING_SUIT, "HEART")));
        assertEquals(5, result.finalMult()); // 只有红桃 10 触发一次
    }

    @Test
    void 四条包含三条触发古怪小丑() {
        var result = calculator.score(
                new Evaluation(HandType.FOUR_OF_A_KIND,
                        List.of(c(Suit.SPADE, Rank.SEVEN), c(Suit.HEART, Rank.SEVEN),
                                c(Suit.CLUB, Rank.SEVEN), c(Suit.DIAMOND, Rank.SEVEN))),
                List.of(joker("wily_joker", ConfiguredSpecialCard.EffectType.ADD_MULT, 12,
                        ConfiguredSpecialCard.ConditionType.HAND_TYPE, "THREE_OF_A_KIND")));
        assertEquals(7 + 12, result.finalMult()); // 四条基础倍率 7
    }

    @Test
    void 同花顺包含顺子触发狂野小丑() {
        var result = calculator.score(
                new Evaluation(HandType.STRAIGHT_FLUSH,
                        List.of(c(Suit.SPADE, Rank.NINE), c(Suit.SPADE, Rank.TEN),
                                c(Suit.SPADE, Rank.JACK), c(Suit.SPADE, Rank.QUEEN),
                                c(Suit.SPADE, Rank.KING))),
                List.of(joker("crazy_joker", ConfiguredSpecialCard.EffectType.ADD_MULT, 12,
                        ConfiguredSpecialCard.ConditionType.HAND_TYPE, "STRAIGHT")));
        assertEquals(8 + 12, result.finalMult()); // 同花顺基础倍率 8
    }

    @Test
    void 部落同花乘算两倍率() {
        var result = calculator.score(
                new Evaluation(HandType.FLUSH,
                        List.of(c(Suit.HEART, Rank.TWO), c(Suit.HEART, Rank.FIVE),
                                c(Suit.HEART, Rank.SEVEN), c(Suit.HEART, Rank.NINE),
                                c(Suit.HEART, Rank.JACK))),
                List.of(joker("tribe", ConfiguredSpecialCard.EffectType.MULTIPLY_MULT, 2,
                        ConfiguredSpecialCard.ConditionType.HAND_TYPE, "FLUSH")));
        assertEquals(4 * 2, result.finalMult()); // 同花基础倍率 4 ×2
    }

    @Test
    void 随机花色永久模式不随关卡变化() {
        var ancient = new ConfiguredSpecialCard("ancient_joker", "古老小丑", "", 8, "",
                ConfiguredSpecialCard.EffectType.MULTIPLY_MULT, 1.5,
                ConfiguredSpecialCard.ConditionType.SCORING_SUIT, "RANDOM",
                ConfiguredSpecialCard.GrowthType.NONE, 0,
                ConfiguredSpecialCard.SuitMode.PERMANENT, 0, 0, 0, 0);
        ancient.onLevelStart();
        Suit first = ancient.currentSuit();
        assertNotNull(first);
        ancient.onLevelStart();
        ancient.onLevelStart();
        assertEquals(first, ancient.currentSuit()); // 永久模式花色不变
    }

    @Test
    void 成长牌描述附带已积累加成() {
        var growing = new ConfiguredSpecialCard("g", "积累筹码", "计分时给予已积累积分", 5, "",
                ConfiguredSpecialCard.EffectType.ADD_CHIPS, 0,
                ConfiguredSpecialCard.ConditionType.ALWAYS, null,
                ConfiguredSpecialCard.GrowthType.PER_HAND, 50);
        assertEquals("计分时给予已积累积分", growing.description());
        growing.onHandPlayed(HandType.PAIR);
        growing.onHandPlayed(HandType.PAIR);
        assertTrue(growing.description().contains("已积累 +100"));
    }

    @Test
    void 随机花色牌描述附带当前花色() {
        var castle = new ConfiguredSpecialCard("castle", "城堡", "每弃牌积累", 6, "",
                ConfiguredSpecialCard.EffectType.ADD_CHIPS, 0,
                ConfiguredSpecialCard.ConditionType.ALWAYS, null,
                ConfiguredSpecialCard.GrowthType.PER_DISCARD, 3,
                ConfiguredSpecialCard.SuitMode.PER_LEVEL, 0, 0, 0, 0);
        castle.onLevelStart();
        assertTrue(castle.description().contains("当前花色"));
    }

    @Test
    void 弃牌成长无花色条件时每张都积累() {
        var card = new ConfiguredSpecialCard("g", "g", "g", 1, "",
                ConfiguredSpecialCard.EffectType.ADD_CHIPS, 0,
                ConfiguredSpecialCard.ConditionType.ALWAYS, null,
                ConfiguredSpecialCard.GrowthType.PER_DISCARD, 3);
        card.onDiscard(List.of(c(Suit.HEART, Rank.TWO), c(Suit.SPADE, Rank.THREE)));
        assertEquals(2, card.growthStacks());
    }

    @Test
    void 重触发不叠加在非目标牌上() {
        var hack = joker("hack", ConfiguredSpecialCard.EffectType.ADD_CHIPS, 1,
                ConfiguredSpecialCard.ConditionType.RETRIGGER_RANKS, "TWO,THREE,FOUR,FIVE");
        assertEquals(1, hack.retriggerCount(c(Suit.SPADE, Rank.THREE), false));
        assertEquals(0, hack.retriggerCount(c(Suit.SPADE, Rank.KING), false));
        assertEquals(0, hack.retriggerCount(null, false));
    }
}
