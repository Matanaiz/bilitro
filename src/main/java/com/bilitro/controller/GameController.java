package com.bilitro.controller;

import com.bilitro.model.card.Card;
import java.util.List;

/**
 * 对局控制器：接收 view 层事件，调用 model，通知 view 刷新。
 * 对应"用户做了什么"：选牌、出牌、弃牌、查看牌组、查看功能牌。
 */
public interface GameController {

    /** 切换某张手牌的选中状态。 */
    void toggleSelect(Card card);

    /** 当前选中的牌。 */
    List<Card> selected();

    /** 点击出牌按钮（内部做规则校验，失败时 view 保持置灰）。 */
    void onPlay();

    /** 点击弃牌按钮（剩余次数为 0 时无响应）。 */
    void onDiscard();

    /** 打开"查看牌组"界面。 */
    void onViewDeck();

    /** 点击功能牌，弹出效果说明浮层。 */
    void onInspectSpecialCard(String specialCardId);
}
