package com.bilitro.model.hand;

import com.bilitro.model.card.Card;
import java.util.List;

/**
 * 牌型判定结果：最高优先级牌型 + 构成该牌型的手牌（按从左往右顺序）。
 *
 * <p>对应计分流程图："从手牌中找出优先级最高的牌型"之后，
 * "在牌型对应手牌中从左往右开始计算分数"——scoringCards 就是参与逐张计分
 * （分数 += 手牌点数、触发功能牌）的那部分牌，未构成牌型的牌不计分。
 * playedCards 是本次打出的全部牌（功能牌"花盆"等按整手判定的效果用）。
 */
public record Evaluation(HandType type, List<Card> scoringCards, List<Card> playedCards) {

    /** 便捷构造：未提供打出牌列表时，打出牌即计分牌。 */
    public Evaluation(HandType type, List<Card> scoringCards) {
        this(type, scoringCards, scoringCards);
    }
}
