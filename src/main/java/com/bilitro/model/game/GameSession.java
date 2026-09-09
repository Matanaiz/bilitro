package com.bilitro.model.game;

import com.bilitro.model.card.Card;
import com.bilitro.model.player.Player;
import java.util.List;

/**
 * 一局游戏的核心模型：关卡进度、手牌、剩余次数、目标分。
 * 纯 Java，不依赖 JavaFX。
 */
public interface GameSession {

    int currentLevel();

    int remainingTargetScore();

    int remainingPlays();

    int remainingDiscards();

    List<Card> hand();

    Player player();

    /** 打出选中的牌：计分、扣减剩余目标、补牌、次数-1。 */
    PlayResult play(List<Card> selected);

    /** 弃牌重抽：次数-1、补牌；次数为 0 时不允许调用。 */
    void discard(List<Card> selected);

    /** 判断当前关卡是否已通关 / 是否已失败。 */
    RoundOutcome outcome();

    /** 进入下一关：加载关卡规则、重置次数与牌组。 */
    void advanceLevel(LevelRule rule);

    /** 一次性出牌结果（得分、牌型），供 view 层做特效。 */
    record PlayResult(HandTypeView handType, int score) {
    }

    /** 供 view 使用的牌型快照（避免 view 依赖 hand 包内部，可调整）。 */
    interface HandTypeView {
        String displayName();
    }

    enum RoundOutcome { ONGOING, LEVEL_CLEARED, FAILED }
}
