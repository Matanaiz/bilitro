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

    private final List<Card> cards = new ArrayList<>();

    public StandardDeck() {
        reset(Set.of());
    }

    @Override
    public List<Card> draw(int n) {
        int count = Math.min(n, cards.size());
        List<Card> drawn = new ArrayList<>(cards.subList(0, count));
        cards.subList(0, count).clear();
        return drawn;
    }

    @Override
    public int remaining() {
        return cards.size();
    }

    @Override
    public List<Card> peekRemaining() {
        return List.copyOf(cards);
    }

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
