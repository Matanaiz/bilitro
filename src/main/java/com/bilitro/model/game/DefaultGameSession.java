package com.bilitro.model.game;

import com.bilitro.model.GameConfig;
import com.bilitro.model.card.Card;
import com.bilitro.model.card.Deck;
import com.bilitro.model.hand.Evaluation;
import com.bilitro.model.hand.HandTypeEvaluator;
import com.bilitro.model.hand.ScoreCalculator;
import com.bilitro.model.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认对局实现：串联 判定 → 计分 → 补牌 → 结束判定 → 奖励结算。
 * 结束判定对应流程图：累计得分 >= 目标 → 第 8 关 VICTORY，否则 LEVEL_CLEARED；
 * 未达标且剩余出牌数 == 0 → FAILED；否则 ONGOING。
 */
public class DefaultGameSession implements GameSession {

    private final Deck deck;
    private final HandTypeEvaluator evaluator;
    private final ScoreCalculator calculator;
    private final Player player;

    private int level = 1;
    private LevelRule rule;
    private int remainingTargetScore;
    private int remainingPlays;
    private int remainingDiscards;
    private int totalScore;
    private final List<Card> hand = new ArrayList<>();

    /** 创建对局并按第一关规则发牌开局。 */
    public DefaultGameSession(Deck deck, HandTypeEvaluator evaluator,
                              ScoreCalculator calculator, Player player,
                              LevelRule firstRule) {
        this.deck = deck;
        this.evaluator = evaluator;
        this.calculator = calculator;
        this.player = player;
        startLevel(firstRule);
    }

    /** 返回当前关卡号（从 1 开始）。 */
    @Override
    public int currentLevel() {
        return level;
    }

    /** 返回距离过关还差的分数。 */
    @Override
    public int remainingTargetScore() {
        return remainingTargetScore;
    }

    /** 返回剩余出牌次数。 */
    @Override
    public int remainingPlays() {
        return remainingPlays;
    }

    /** 返回剩余弃牌次数。 */
    @Override
    public int remainingDiscards() {
        return remainingDiscards;
    }

    /** 返回本局累计得分（结算与最高分记录用）。 */
    @Override
    public int totalScore() {
        return totalScore;
    }

    /** 返回当前手牌的只读快照。 */
    @Override
    public List<Card> hand() {
        return List.copyOf(hand);
    }

    /** 返回本局玩家（货币与功能牌栏）。 */
    @Override
    public Player player() {
        return player;
    }

    /**
     * 打出选中的牌：判定牌型 → 计分（含功能牌）→ 扣减剩余目标分与出牌次数
     * → 累加总分 → 移除已出牌并补牌，返回本次出牌结果供界面做特效。
     */
    @Override
    public PlayResult play(List<Card> selected) {
        Evaluation eval = evaluator.evaluateDetail(selected)
                .orElseThrow(() -> new IllegalArgumentException("所选牌不满足出牌规则"));
        if (remainingPlays <= 0) {
            throw new IllegalStateException("剩余出牌次数为 0");
        }
        var breakdown = calculator.score(eval, player.specialCards());
        remainingPlays--;
        remainingTargetScore -= breakdown.finalScore();
        totalScore += breakdown.finalScore();
        replaceCards(selected);
        return new PlayResult(eval.type(), breakdown.finalScore());
    }

    /** 弃掉选中的牌并补等量新牌；弃牌次数为 0 时抛异常。 */
    @Override
    public void discard(List<Card> selected) {
        if (remainingDiscards <= 0) {
            throw new IllegalStateException("剩余弃牌次数为 0");
        }
        remainingDiscards--;
        replaceCards(selected);
    }

    /** 按结束判定流程图判断当前对局结局。 */
    @Override
    public RoundOutcome outcome() {
        if (remainingTargetScore <= 0) {
            return level >= GameConfig.MAX_LEVEL ? RoundOutcome.VICTORY : RoundOutcome.LEVEL_CLEARED;
        }
        return remainingPlays <= 0 ? RoundOutcome.FAILED : RoundOutcome.ONGOING;
    }

    /** 结算并发放通关奖励：固定奖励 + 剩余出牌奖励 + 利息，返回入账总额。 */
    @Override
    public int claimLevelClearReward() {
        int reward = GameConfig.LEVEL_CLEAR_REWARD
                + remainingPlays * GameConfig.COIN_PER_REMAINING_PLAY
                + player.coins() / GameConfig.INTEREST_EVERY_N_COINS;
        player.addCoins(reward);
        return reward;
    }

    /** 进入下一关：关卡号加一并按新规则重置本关状态。 */
    @Override
    public void advanceLevel(LevelRule rule) {
        level++;
        startLevel(rule);
    }

    /** 加载关卡规则：重置目标分、次数、牌组与手牌。 */
    private void startLevel(LevelRule rule) {
        this.rule = rule;
        this.remainingTargetScore = rule.targetScore();
        this.remainingPlays = GameConfig.PLAYS_PER_LEVEL;
        this.remainingDiscards = GameConfig.DISCARDS_PER_LEVEL;
        deck.reset(rule.bannedSuits());
        hand.clear();
        hand.addAll(deck.draw(GameConfig.HAND_SIZE));
    }

    /** 从手牌移除已使用的牌，并从牌组补等量牌。 */
    private void replaceCards(List<Card> used) {
        hand.removeAll(used);
        hand.addAll(deck.draw(used.size()));
    }
}
