package com.bilitro.model.player;

import com.bilitro.model.GameConfig;

import java.util.ArrayList;
import java.util.List;

/** 默认玩家实现：货币 + 功能牌栏（上限见 GameConfig.SPECIAL_CARD_LIMIT）。 */
public class DefaultPlayer implements Player {

    private int coins;
    private final List<SpecialCard> specialCards = new ArrayList<>();

    public DefaultPlayer() {
        this(GameConfig.INITIAL_COINS);
    }

    public DefaultPlayer(int initialCoins) {
        this.coins = initialCoins;
    }

    @Override
    public int coins() {
        return coins;
    }

    @Override
    public void addCoins(int delta) {
        coins += delta;
    }

    @Override
    public List<SpecialCard> specialCards() {
        return List.copyOf(specialCards);
    }

    @Override
    public boolean addSpecialCard(SpecialCard card) {
        if (specialCards.size() >= GameConfig.SPECIAL_CARD_LIMIT) {
            return false;
        }
        return specialCards.add(card);
    }

    @Override
    public boolean removeSpecialCard(SpecialCard card) {
        return specialCards.remove(card);
    }
}
