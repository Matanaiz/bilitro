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

    /** 本关的目标得分（固定值，一关内不随出牌变化）。 */
    int targetScore();

    int remainingTargetScore();

    int remainingPlays();

    int remainingDiscards();

    List<Card> hand();

    /** 牌组剩余牌的只读快照（需求 2.2.6 查看牌组）。 */
    List<Card> remainingDeck();

    Player player();

    /** 打出选中的牌：计分、扣减剩余目标、补牌、次数-1。 */
    PlayResult play(List<Card> selected);

    /** 弃牌重抽：次数-1、补牌；次数为 0 时不允许调用。 */
    void discard(List<Card> selected);

    /** 本关累计得分（每关开始清零；最高分只记录最后一关的通关得分）。 */
    int levelScore();

    /**
     * 结束判定（见结束判定流程图）：
     * 累计得分 >= 目标得分 → 第 8 关则 VICTORY，否则 LEVEL_CLEARED；
     * 未达标且剩余出牌数 == 0 → FAILED；否则 ONGOING。
     */
    RoundOutcome outcome();

    /**
     * 领取通关奖励（答复 5）：固定 4 代币 + 每剩余 1 次出牌 1 代币
     * + 上回合每剩余 5 代币 1 代币利息。返回奖励明细（含各项与总额）。
     */
    RewardBreakdown claimLevelClearReward();

    /** 通关奖励明细：固定过关奖励、剩余出牌奖励、利息奖励、功能牌奖励（如"黄金小丑"）。 */
    record RewardBreakdown(int base, int playBonus, int interest, int cardBonus) {
        /** 奖励总额 = 四项之和。 */
        public int total() {
            return base + playBonus + interest + cardBonus;
        }
    }

    /** 进入下一关：加载关卡规则、重置次数与牌组。 */
    void advanceLevel(LevelRule rule);

    /** 一次性出牌结果（得分、牌型），供 view 层做特效。 */
    record PlayResult(HandTypeView handType, int score) {
    }

    /** 供 view 使用的牌型快照（避免 view 依赖 hand 包内部，可调整）。 */
    interface HandTypeView {
        String displayName();
    }

    enum RoundOutcome { ONGOING, LEVEL_CLEARED, VICTORY, FAILED }
}
