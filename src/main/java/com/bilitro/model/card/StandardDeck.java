package com.bilitro.model.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 标准 52 张牌组（无大小王，答复 8）。
 * 洗牌、抽牌、按禁用花色重置。
 */
public class StandardDeck implements Deck {

    /** 抽牌堆，栈顶为列表头部。 */
    private final List<Card> cards = new ArrayList<>();

    /** 构造一副完整的 52 张牌组并洗牌。 */
    public StandardDeck() {
        reset(Set.of());
    }

    /** 从牌堆顶部随机抽出 n 张；不足 n 张时全部抽出。 */
    @Override
    public List<Card> draw(int n) {
        int count = Math.min(n, cards.size());
        List<Card> drawn = new ArrayList<>(cards.subList(0, count));
        cards.subList(0, count).clear();
        return drawn;
    }

    /** 返回牌堆剩余牌数。 */
    @Override
    public int remaining() {
        return cards.size();
    }

    /** 返回剩余牌的只读快照，供"查看牌组"界面展示。 */
    @Override
    public List<Card> peekRemaining() {
        return List.copyOf(cards);
    }

    /** 重置为完整牌组并洗牌；被禁用的花色不进入牌堆（关卡规则用）。 */
    @Override
    public void reset(Set<Suit> bannedSuits) {
        cards.clear();
        for (Suit suit : Suit.values()) {
            if (bannedSuits.contains(suit)) {
                continue;
            }
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(cards);
    }
}
