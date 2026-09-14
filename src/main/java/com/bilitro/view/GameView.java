package com.bilitro.view;

import com.bilitro.model.card.Card;
import com.bilitro.model.hand.ScoreCalculator;
import com.bilitro.model.player.SpecialCard;
import java.util.List;

/**
 * 对局视图：只管长什么样。JavaFX 相关代码只出现在 view 层。
 */
public interface GameView {

    /** 刷新手牌区。 */
    void renderHand(List<Card> hand, List<Card> selected, List<Card> scoringCards);

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

    /** 刷新计分预览：显示选中牌型的初始分数与初始倍数；无可出牌型时清空。 */
    void renderPreview(String handTypeName, Integer chips, Integer mult);

    /** 刷新关卡进度：当前关卡与本关累计得分。 */
    void renderProgress(int level, int totalScore);

    /** 刷新剩余牌组张数显示。 */
    void renderDeckCount(int remaining);

    /** 刷新功能牌栏（六槽位）。 */
    void renderSpecialCards(List<SpecialCard> specials);

    /** 清空中间计分过程区（通关/刷新时调用）。 */
    void clearProcess();

    /**
     * 播放计分过程：在中间区域从左到右逐张摆出计分手牌，
     * 左侧当前积分与当前倍数随每一步增长；全部播完后执行 onFinished。
     */
    void playScoringProcess(List<ScoreCalculator.ScoreStep> steps, Runnable onFinished);
}
