package com.bilitro.model.hand;

import com.bilitro.model.card.Card;
import com.bilitro.model.player.SpecialCard;
import java.util.List;

/**
 * 计分器：得分 = 牌型基础分 × 倍数 + 功能牌加成。
 * 对应需求 2.1.2 自动计分。
 */
public interface ScoreCalculator {

    /**
     * 计算一次出牌的得分。
     * @param type     已判定的牌型
     * @param played   打出的牌
     * @param specials 当前持有的功能牌
     * @return 最终得分
     */
    int score(HandType type, List<Card> played, List<SpecialCard> specials);

    // TODO: 基础分/倍数表来源待确认（硬编码 or 配置文件）
}
