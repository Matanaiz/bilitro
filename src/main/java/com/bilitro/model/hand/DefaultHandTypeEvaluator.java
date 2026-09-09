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

    @Override
    public Optional<HandType> evaluate(List<Card> selected) {
        return evaluateDetail(selected).map(Evaluation::type);
    }

    @Override
    public Optional<Evaluation> evaluateDetail(List<Card> selected) {
        if (!isPlayable(selected)) {
            return Optional.empty();
        }
        List<Card> cards = new ArrayList<>(selected);
        Map<Rank, List<Card>> byRank = groupByRank(cards);
        boolean flush = cards.size() == 5
                && cards.stream().allMatch(c -> c.suit() == cards.get(0).suit());
        boolean straight = cards.size() == 5 && isStraight(cards);

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
        if (trips != null) {
            return Optional.of(new Evaluation(HandType.THREE_OF_A_KIND, trips));
        }
        List<Card> twoPair = twoPairs(byRank);
        if (twoPair != null) {
            return Optional.of(new Evaluation(HandType.TWO_PAIR, twoPair));
        }
        if (pairs != null) {
            return Optional.of(new Evaluation(HandType.PAIR, pairs));
        }
        Card highest = cards.stream()
                .max(Comparator.comparingInt(c -> c.rank().value()))
                .orElseThrow();
        return Optional.of(new Evaluation(HandType.HIGH_CARD, List.of(highest)));
    }

    @Override
    public boolean isPlayable(List<Card> selected) {
        return selected != null
                && selected.size() >= GameConfig.MIN_SELECT
                && selected.size() <= GameConfig.MAX_SELECT;
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

    /** 是否 5 张连续（A 只算 14，不算 1）。 */
    private boolean isStraight(List<Card> cards) {
        List<Integer> values = cards.stream()
                .map(c -> c.rank().value())
                .sorted()
                .distinct()
                .toList();
        if (values.size() != 5) {
            return false;
        }
        return values.get(4) - values.get(0) == 4;
    }
}
