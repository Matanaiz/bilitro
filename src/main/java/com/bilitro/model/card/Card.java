package com.bilitro.model.card;

/**
 * 扑克牌：纯数据对象，不可变。
 * 纯 Java，不依赖任何 JavaFX 类。
 */
public record Card(Suit suit, Rank rank) {
    // TODO: 是否需要在 Card 上携带皮肤 id / 临时强化状态（被功能牌修改），待确认
}
