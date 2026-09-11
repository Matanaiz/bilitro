package com.bilitro.view;

import com.bilitro.model.card.Card;
import java.util.List;

/**
 * 对局视图：只管长什么样。JavaFX 相关代码只出现在 view 层。
 */
public interface GameView {

    /** 刷新手牌区。 */
    void renderHand(List<Card> hand, List<Card> selected);

    /** 刷新剩余目标分、剩余出牌/弃牌次数、货币。 */
    void renderStatus(int targetScore, int plays, int discards, int coins);

    /** 按规则校验结果亮/灰出牌与弃牌按钮。 */
    void setActionEnabled(boolean canPlay, boolean canDiscard);

    /** 播放计分特效（P2，特效强度与得分量级匹配）。 */
    void playScoreEffect(int score);

    /** 弹功能牌说明浮层（再次点击或点关闭后消失）。 */
    void showSpecialCardTip(String description);

    /** 弹剩余牌组查看浮层（需求 2.2.6）。 */
    void showDeck(List<Card> remaining);

    /** 弹通用提示（过关/胜利/失败等，P0 用文本，P2 换成动画）。 */
    void showMessage(String message);

    /** 弹通关结算弹窗：本关得分与代币奖励明细（过关奖励、剩余出牌奖励、利息奖励）。 */
    void showLevelClearReward(int levelScore, com.bilitro.model.game.GameSession.RewardBreakdown reward);
}
