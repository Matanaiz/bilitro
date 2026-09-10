package com.bilitro.model.hand;

import com.bilitro.model.card.Card;
import com.bilitro.model.card.Rank;
import com.bilitro.model.card.Suit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 牌型判定器测试：覆盖九种牌型与边界规则。 */
class DefaultHandTypeEvaluatorTest {

    private final HandTypeEvaluator evaluator = new DefaultHandTypeEvaluator();

    /** 构造一张牌的简写。 */
    private static Card c(Suit suit, Rank rank) {
        return new Card(suit, rank);
    }

    @Test
    void 同花顺() {
        List<Card> cards = List.of(
                c(Suit.SPADE, Rank.NINE), c(Suit.SPADE, Rank.TEN),
                c(Suit.SPADE, Rank.JACK), c(Suit.SPADE, Rank.QUEEN),
                c(Suit.SPADE, Rank.KING));
        var eval = evaluator.evaluateDetail(cards).orElseThrow();
        assertEquals(HandType.STRAIGHT_FLUSH, eval.type());
        assertEquals(5, eval.scoringCards().size());
    }

    @Test
    void 四条只计四张同点牌() {
        List<Card> cards = List.of(
                c(Suit.SPADE, Rank.SEVEN), c(Suit.HEART, Rank.SEVEN),
                c(Suit.CLUB, Rank.SEVEN), c(Suit.DIAMOND, Rank.SEVEN),
                c(Suit.SPADE, Rank.TWO));
        var eval = evaluator.evaluateDetail(cards).orElseThrow();
        assertEquals(HandType.FOUR_OF_A_KIND, eval.type());
        assertEquals(4, eval.scoringCards().size());
    }

    @Test
    void 葫芦() {
        List<Card> cards = List.of(
                c(Suit.SPADE, Rank.THREE), c(Suit.HEART, Rank.THREE),
                c(Suit.CLUB, Rank.THREE), c(Suit.DIAMOND, Rank.FIVE),
                c(Suit.SPADE, Rank.FIVE));
        var eval = evaluator.evaluateDetail(cards).orElseThrow();
        assertEquals(HandType.FULL_HOUSE, eval.type());
        assertEquals(5, eval.scoringCards().size());
    }

    @Test
    void 同花() {
        List<Card> cards = List.of(
                c(Suit.HEART, Rank.TWO), c(Suit.HEART, Rank.FIVE),
                c(Suit.HEART, Rank.SEVEN), c(Suit.HEART, Rank.NINE),
                c(Suit.HEART, Rank.KING));
        assertEquals(HandType.FLUSH, evaluator.evaluate(cards).orElseThrow());
    }

    @Test
    void 顺子() {
        List<Card> cards = List.of(
                c(Suit.SPADE, Rank.FOUR), c(Suit.HEART, Rank.FIVE),
                c(Suit.CLUB, Rank.SIX), c(Suit.DIAMOND, Rank.SEVEN),
                c(Suit.SPADE, Rank.EIGHT));
        assertEquals(HandType.STRAIGHT, evaluator.evaluate(cards).orElseThrow());
    }

    @Test
    void A2345不算顺子() {
        List<Card> cards = List.of(
                c(Suit.SPADE, Rank.ACE), c(Suit.HEART, Rank.TWO),
                c(Suit.CLUB, Rank.THREE), c(Suit.DIAMOND, Rank.FOUR),
                c(Suit.SPADE, Rank.FIVE));
        assertNotEquals(HandType.STRAIGHT, evaluator.evaluate(cards).orElseThrow());
    }

    @Test
    void 三条只计三张() {
        List<Card> cards = List.of(
                c(Suit.SPADE, Rank.NINE), c(Suit.HEART, Rank.NINE),
                c(Suit.CLUB, Rank.NINE), c(Suit.DIAMOND, Rank.TWO),
                c(Suit.SPADE, Rank.FOUR));
        var eval = evaluator.evaluateDetail(cards).orElseThrow();
        assertEquals(HandType.THREE_OF_A_KIND, eval.type());
        assertEquals(3, eval.scoringCards().size());
    }

    @Test
    void 两对() {
        List<Card> cards = List.of(
                c(Suit.SPADE, Rank.NINE), c(Suit.HEART, Rank.NINE),
                c(Suit.CLUB, Rank.FOUR), c(Suit.DIAMOND, Rank.FOUR),
                c(Suit.SPADE, Rank.SIX));
        var eval = evaluator.evaluateDetail(cards).orElseThrow();
        assertEquals(HandType.TWO_PAIR, eval.type());
        assertEquals(4, eval.scoringCards().size());
    }

    @Test
    void 对子只计两张() {
        List<Card> cards = List.of(
                c(Suit.SPADE, Rank.TEN), c(Suit.HEART, Rank.TEN),
                c(Suit.CLUB, Rank.FOUR));
        var eval = evaluator.evaluateDetail(cards).orElseThrow();
        assertEquals(HandType.PAIR, eval.type());
        assertEquals(2, eval.scoringCards().size());
    }

    @Test
    void 高牌只计最大一张() {
        List<Card> cards = List.of(
                c(Suit.SPADE, Rank.TWO), c(Suit.HEART, Rank.FIVE),
                c(Suit.CLUB, Rank.ACE));
        var eval = evaluator.evaluateDetail(cards).orElseThrow();
        assertEquals(HandType.HIGH_CARD, eval.type());
        assertEquals(1, eval.scoringCards().size());
        assertEquals(Rank.ACE, eval.scoringCards().get(0).rank());
    }

    @Test
    void 少选多选不合法() {
        assertFalse(evaluator.isPlayable(List.of()));
        List<Card> six = List.of(
                c(Suit.SPADE, Rank.TWO), c(Suit.HEART, Rank.THREE),
                c(Suit.CLUB, Rank.FOUR), c(Suit.DIAMOND, Rank.FIVE),
                c(Suit.SPADE, Rank.SIX), c(Suit.HEART, Rank.SEVEN));
        assertFalse(evaluator.isPlayable(six));
        assertTrue(evaluator.evaluateDetail(six).isEmpty());
    }
}
