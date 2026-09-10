package com.bilitro.model.player;

import com.bilitro.model.GameConfig;

import java.util.ArrayList;
import java.util.List;

/** 默认玩家实现：货币 + 功能牌栏（上限见 GameConfig.SPECIAL_CARD_LIMIT）。 */
public class DefaultPlayer implements Player {

    /** 当前持有代币数。 */
    private int coins;

    /** 已购买的功能牌。 */
    private final List<SpecialCard> specialCards = new ArrayList<>();

    /** 以初始货币创建玩家（见 GameConfig.INITIAL_COINS）。 */
    public DefaultPlayer() {
        this(GameConfig.INITIAL_COINS);
    }

    /** 以指定货币数创建玩家。 */
    public DefaultPlayer(int initialCoins) {
        this.coins = initialCoins;
    }

    /** 返回当前代币数。 */
    @Override
    public int coins() {
        return coins;
    }

    /** 增加（或扣减，delta 为负时）代币。 */
    @Override
    public void addCoins(int delta) {
        coins += delta;
    }

    /** 返回当前持有的功能牌只读列表。 */
    @Override
    public List<SpecialCard> specialCards() {
        return List.copyOf(specialCards);
    }

    /** 添加功能牌到栏位；超过栏位上限时返回 false 且不添加。 */
    @Override
    public boolean addSpecialCard(SpecialCard card) {
        if (specialCards.size() >= GameConfig.SPECIAL_CARD_LIMIT) {
            return false;
        }
        return specialCards.add(card);
    }

    /** 移除指定功能牌；不存在时返回 false。 */
    @Override
    public boolean removeSpecialCard(SpecialCard card) {
        return specialCards.remove(card);
    }
}
