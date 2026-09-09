package com.bilitro.model.game;

import com.bilitro.model.card.Suit;
import java.util.Set;

/**
 * 关卡差异化规则（需求 2.2.9）。
 * 答复 9：除"禁用花色"外的更多规则（如调整出牌/弃牌次数、限制牌型）稍后具体添加；
 * 届时可扩展本 record 字段或改为规则列表以支持叠加。
 */
public record LevelRule(
        int targetScore,
        Set<Suit> bannedSuits,
        String hintText // 进入关卡时的规则提示
) {
}
