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

    /**
     * 一次出牌计分完成后由对局调用：成长类功能牌在此积累永久加成
     * （如"每打出一次，本牌效果永久 +50 积分"）。
     * 默认无成长；由具体实现覆写。
     *
     * @param handType 本次出牌的牌型
     */
    default void onHandPlayed(com.bilitro.model.hand.HandType handType) {
    }

    /**
     * 一次弃牌完成后由对局调用：弃牌成长类功能牌在此积累
     * （如"城堡"：每弃掉 1 张指定花色的牌 +3 积分）。
     */
    default void onDiscard(java.util.List<com.bilitro.model.card.Card> discarded) {
    }

    /** 每关开始时由对局调用：按回合变化的效果在此重置（如随机花色功能牌换花色）。 */
    default void onLevelStart() {
    }

    /** 元规则：返回 true 表示所有打出的牌都参与计分（"飞溅"）。 */
    default boolean allCardsScore() {
        return false;
    }

    /** 元规则：返回 true 表示红桃=方块、梅花=黑桃（"模糊小丑"，影响同花判定）。 */
    default boolean mergesSuits() {
        return false;
    }

    /** 被动：每关弃牌次数修正（可为负）。 */
    default int discardsDelta() {
        return 0;
    }

    /** 被动：手牌上限修正（可为负）。 */
    default int handSizeDelta() {
        return 0;
    }

    /** 被动：商店可负债额度（"信用卡"：可负债至 -20）。 */
    default int creditLimit() {
        return 0;
    }

    /** 被动：每关通关时额外获得的代币（"黄金小丑"：回合结束 +4）。 */
    default int levelClearCoins() {
        return 0;
    }

    /**
     * 随机花色类功能牌（古老小丑、城堡）当前生效的花色；非随机花色牌返回 null。
     * 界面用来在牌面上直接显示花色标记。
     */
    default com.bilitro.model.card.Suit currentSuit() {
        return null;
    }
}
