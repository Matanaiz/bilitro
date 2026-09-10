package com.bilitro.model.player;

import com.bilitro.model.hand.ScoringContext;

/**
 * 功能牌（可购买的构筑牌，不可升级——小组答复 12）。
 * 效果数据以配置表存储（需求 5.1 假设 7），具体效果实现方式（策略类/枚举）待小组拍板（答复 11）。
 */
public interface SpecialCard {

    /** 配置表中的唯一 id。 */
    String id();

    /** 效果描述文本（用于 2.2.5 效果查看浮层，与配置表描述字段一致）。 */
    String description();

    /** 价格（商店购买用）。 */
    int price();

    /**
     * 计分时触发（计分流程图：逐张手牌计分后"触发功能牌"）。
     * 通过 {@link ScoringContext#addChips} / {@code addMult} / {@code multiplyMult} 修改分数与倍数。
     * 默认无效果；由具体效果实现类覆写。
     */
    default void onScore(ScoringContext ctx) {
    }

    /**
     * 触发时机：true 表示逐张手牌触发（如"每张红桃牌计分时"类效果）；
     * false 表示整手结算时只触发一次（如无条件的"小丑 +4 倍率"）。
     */
    default boolean triggerPerCard() {
        return true;
    }

    /**
     * 重触发：返回当前这张计分手牌应额外计分的次数（0 表示计分一次，不重触发）。
     * 例如"重触发每张 2/3/4/5"对这些牌返回 1，则该牌的点数与逐张效果重复结算两次。
     *
     * @param card 当前计分手牌
     * @param allFaceCards 是否所有牌均视为人头牌（"幻视"效果）
     */
    default int retriggerCount(com.bilitro.model.card.Card card, boolean allFaceCards) {
        return 0;
    }

    /**
     * 元规则：返回 true 表示持有本牌期间所有手牌均视为人头牌（"幻视"）。
     * 影响人头牌条件与"重触发人头牌"的判定。
     */
    default boolean allCardsFace() {
        return false;
    }

    // TODO(答复 1/3): 手牌数量、出牌/弃牌次数的修改类效果，建议在配置表中加
    //  passive 修饰字段（如 handSizeDelta / playsDelta），由 GameSession 开局时汇总生效。
}
