package com.bilitro.model.player;

import java.util.List;

/** 玩家状态：货币 + 持有的功能牌。 */
public interface Player {

    int coins();

    void addCoins(int delta);

    List<SpecialCard> specialCards();

    /** 添加功能牌；超过栏位上限时返回 false。 */
    boolean addSpecialCard(SpecialCard card);

    boolean removeSpecialCard(SpecialCard card);
}
