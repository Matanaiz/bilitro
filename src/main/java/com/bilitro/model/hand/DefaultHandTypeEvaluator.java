package com.bilitro.model.hand;

import com.bilitro.model.GameConfig;
import com.bilitro.model.card.Card;
import com.bilitro.model.card.Rank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 默认牌型判定器：按优先级从高到低查表判定。
 * 规则：选中 1~5 张；顺子/同花/葫芦/同花顺需 5 张；
 * A 最大（14），不允许 A-2-3-4-5 顺子（答复 7）。
 * 未构成牌型的牌不参与计分（高牌只计最大的一张）。
 */
public class DefaultHandTypeEvaluator implements HandTypeEvaluator {

    /** 判定这组牌构成的最高牌型；不满足出牌规则时返回 empty。 */
    @Override
    public Optional<HandType> evaluate(List<Card> selected) {
        return evaluateDetail(selected).map(Evaluation::type);
    }

    /** 完整判定：从最高牌型开始逐级尝试，返回牌型与参与计分的手牌。 */
    @Override
    public Optional<Evaluation> evaluateDetail(List<Card> selected) {
        if (!isPlayable(selected)) {
            return Optional.empty();
        }
        List<Card> cards = new ArrayList<>(selected);
        Map<Rank, List<Card>> byRank = groupByRank(cards);

        Optional<Evaluation> result = matchFiveCardTypes(cards, byRank);
        if (result.isPresent()) {
            return result;
        }
        return matchRankGroups(cards, byRank);
    }

    /** 校验选中张数是否在出牌规则范围内（1~5 张）。 */
    @Override
    public boolean isPlayable(List<Card> selected) {
        return selected != null
                && selected.size() >= GameConfig.MIN_SELECT
                && selected.size() <= GameConfig.MAX_SELECT;
    }

    /**
     * 匹配需要 5 张牌的牌型：同花顺、四条、葫芦、同花、顺子。
     * 不足 5 张或不构成时返回 empty。
     */
    private Optional<Evaluation> matchFiveCardTypes(List<Card> cards, Map<Rank, List<Card>> byRank) {
        boolean flush = isFlush(cards);
        boolean straight = isStraight(cards);
        if (straight && flush) {
            return Optional.of(new Evaluation(HandType.STRAIGHT_FLUSH, cards));
        }
        List<Card> quads = ofAKind(byRank, 4);
        if (quads != null) {
            return Optional.of(new Evaluation(HandType.FOUR_OF_A_KIND, quads));
        }
        List<Card> trips = ofAKind(byRank, 3);
        List<Card> pairs = ofAKind(byRank, 2);
        if (trips != null && pairs != null) {
            List<Card> fullHouse = new ArrayList<>(trips);
            fullHouse.addAll(pairs);
            return Optional.of(new Evaluation(HandType.FULL_HOUSE, fullHouse));
        }
        if (flush) {
            return Optional.of(new Evaluation(HandType.FLUSH, cards));
        }
        if (straight) {
            return Optional.of(new Evaluation(HandType.STRAIGHT, cards));
        }
        return Optional.empty();
    }

    /**
     * 匹配只依赖点数重复的牌型：三条、两对、对子；
     * 都不构成时按高牌处理（只计最大的一张）。
     */
    private Optional<Evaluation> matchRankGroups(List<Card> cards, Map<Rank, List<Card>> byRank) {
        List<Card> trips = ofAKind(byRank, 3);
        if (trips != null) {
            return Optional.of(new Evaluation(HandType.THREE_OF_A_KIND, trips));
        }
        List<Card> twoPair = twoPairs(byRank);
        if (twoPair != null) {
            return Optional.of(new Evaluation(HandType.TWO_PAIR, twoPair));
        }
        List<Card> pairs = ofAKind(byRank, 2);
        if (pairs != null) {
            return Optional.of(new Evaluation(HandType.PAIR, pairs));
        }
        return Optional.of(new Evaluation(HandType.HIGH_CARD, List.of(highestCard(cards))));
    }

    /** 判断是否为同花：5 张且花色全部相同。 */
    private boolean isFlush(List<Card> cards) {
        return cards.size() == 5
                && cards.stream().allMatch(c -> c.suit() == cards.get(0).suit());
    }

    /** 判断是否为顺子：5 张点数连续（A 只算 14，不算 1）。 */
    private boolean isStraight(List<Card> cards) {
        if (cards.size() != 5) {
            return false;
        }
        List<Integer> values = cards.stream()
                .map(c -> c.rank().value())
                .sorted()
                .distinct()
                .toList();
        return values.size() == 5 && values.get(4) - values.get(0) == 4;
    }

    /** 返回点数最大的那张牌（高牌计分用）。 */
    private Card highestCard(List<Card> cards) {
        return cards.stream()
                .max(Comparator.comparingInt(c -> c.rank().value()))
                .orElseThrow();
    }

    /** 按点数分组，保持牌在原选中列表中的先后顺序（从左往右）。 */
    private Map<Rank, List<Card>> groupByRank(List<Card> cards) {
        Map<Rank, List<Card>> map = new HashMap<>();
        for (Card c : cards) {
            map.computeIfAbsent(c.rank(), k -> new ArrayList<>()).add(c);
        }
        return map;
    }

    /** 找出 n 张同点数的牌（点数最大的那一组）；没有则返回 null。 */
    private List<Card> ofAKind(Map<Rank, List<Card>> byRank, int n) {
        return byRank.entrySet().stream()
                .filter(e -> e.getValue().size() == n)
                .max(Map.Entry.comparingByKey(Comparator.comparingInt(Rank::value)))
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    /** 找出两对（按点数从大到小取两组）；没有则返回 null。 */
    private List<Card> twoPairs(Map<Rank, List<Card>> byRank) {
        List<List<Card>> pairs = byRank.values().stream()
                .filter(g -> g.size() == 2)
                .toList();
        if (pairs.size() < 2) {
            return null;
        }
        List<Card> result = new ArrayList<>(pairs.get(0));
        result.addAll(pairs.get(1));
        return result;
    }
}
