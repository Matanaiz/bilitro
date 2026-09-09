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

    // TODO(答复 1/3): 手牌数量、出牌/弃牌次数的修改类效果，建议在配置表中加
    //  passive 修饰字段（如 handSizeDelta / playsDelta），由 GameSession 开局时汇总生效。
}
