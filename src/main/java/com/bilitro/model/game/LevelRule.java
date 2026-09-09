package com.bilitro.model.game;

import com.bilitro.model.card.Suit;
import java.util.Set;

/**
 * 关卡差异化规则（需求 2.2.9）。
 * TODO: 目前仅示例"禁用花色"，更多规则类型（如限制牌型、调整次数）待定义，
 *  可考虑改为规则接口列表以支持叠加。
 */
public record LevelRule(
        int targetScore,
        Set<Suit> bannedSuits,
        String hintText // 进入关卡时的规则提示
) {
}
