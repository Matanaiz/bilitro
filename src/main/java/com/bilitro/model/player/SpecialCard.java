package com.bilitro.model.player;

/**
 * 功能牌（可购买/升级的构筑牌）。
 * TODO: 效果以配置表存储，字段需小组设计（需求 5.1 假设 7）。
 *  目前先以 id + 描述占位，效果实现方式（枚举硬编码 / 脚本 / 策略类）待确认。
 */
public interface SpecialCard {

    /** 配置表中的唯一 id。 */
    String id();

    /** 效果描述文本（用于 2.2.5 效果查看浮层，与配置表描述字段一致）。 */
    String description();

    /** 价格（商店购买用）。 */
    int price();

    // TODO: 升级机制（等级、升级费用、数值成长）待确认
}
