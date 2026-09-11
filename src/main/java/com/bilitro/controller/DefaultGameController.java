package com.bilitro.controller;

import com.bilitro.model.GameConfig;
import com.bilitro.model.card.Card;
import com.bilitro.model.game.GameSession;
import com.bilitro.model.hand.HandTypeEvaluator;
import com.bilitro.model.hand.ScoreCalculator;
import com.bilitro.model.player.SpecialCard;
import com.bilitro.view.GameViewFx;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 默认对局控制器：接收界面事件，调用模型，驱动界面刷新。
 * 通关/胜利/失败的场景切换通过 outcomeHandler 回调交给 App（控制器不碰 JavaFX）。
 */
public class DefaultGameController implements GameController {

    private final GameSession session;
    private final HandTypeEvaluator evaluator;
    private final ScoreCalculator calculator;
    private final GameViewFx view;
    private final List<Card> selected = new ArrayList<>();

    /** 对局结局回调（LEVEL_CLEARED/VICTORY/FAILED），由 App 注入做场景切换。 */
    private Consumer<GameSession.RoundOutcome> outcomeHandler = o -> { };

    /** 创建控制器并刷新一次界面。 */
    public DefaultGameController(GameSession session, HandTypeEvaluator evaluator,
                                 ScoreCalculator calculator, GameViewFx view) {
        this.session = session;
        this.evaluator = evaluator;
        this.calculator = calculator;
        this.view = view;
    }

    /** 注入对局结局回调。 */
    public void setOutcomeHandler(Consumer<GameSession.RoundOutcome> handler) {
        this.outcomeHandler = handler;
    }

    /** 切换某张手牌的选中状态，并按规则刷新按钮亮灰。 */
    @Override
    public void toggleSelect(Card card) {
        if (!selected.remove(card) && selected.size() < GameConfig.MAX_SELECT) {
            selected.add(card);
        }
        // 超过选中上限时不加选，但仍刷新界面，把误高亮的卡牌控件还原
        refresh();
    }

    /** 返回当前选中的牌。 */
    @Override
    public List<Card> selected() {
        return List.copyOf(selected);
    }

    /** 出牌：先在中间区域逐张播放计分过程，播完再落账并检查结局。 */
    @Override
    public void onPlay() {
        var eval = evaluate(selected);
        if (eval.isEmpty()) {
            return;
        }
        var steps = calculator.steps(eval.get(), session.player().specialCards());
        List<Card> played = List.copyOf(selected);
        selected.clear();
        view.setActionEnabled(false, false); // 动画期间锁定操作
        view.playScoringProcess(steps, () -> {
            var result = session.play(played);
            view.playScoreEffect(result.score());
            refresh();
            checkOutcome();
        });
    }

    /**
     * 判定选中牌（与对局口径一致）：花色归并（模糊小丑）参与牌型判定；
     * 持有"飞溅"时计分牌扩展为全部打出的牌。
     */
    private java.util.Optional<com.bilitro.model.hand.Evaluation> evaluate(List<Card> cards) {
        boolean merge = session.player().specialCards().stream().anyMatch(SpecialCard::mergesSuits);
        var eval = evaluator.evaluateDetail(cards, merge);
        boolean splash = session.player().specialCards().stream().anyMatch(SpecialCard::allCardsScore);
        if (splash && eval.isPresent()) {
            return java.util.Optional.of(new com.bilitro.model.hand.Evaluation(
                    eval.get().type(), List.copyOf(cards), List.copyOf(cards)));
        }
        return eval;
    }

    /** 弃牌：次数为 0 时无响应。 */
    @Override
    public void onDiscard() {
        if (session.remainingDiscards() <= 0 || selected.isEmpty()) {
            return;
        }
        session.discard(selected);
        selected.clear();
        refresh();
    }

    /** 打开查看牌组浮层。 */
    @Override
    public void onViewDeck() {
        view.showDeck(session.remainingDeck());
    }

    /** 弹出功能牌效果说明。 */
    @Override
    public void onInspectSpecialCard(String specialCardId) {
        session.player().specialCards().stream()
                .filter(s -> s.id().equals(specialCardId))
                .findFirst()
                .map(SpecialCard::description)
                .ifPresent(view::showSpecialCardTip);
    }

    /** 全量刷新界面：手牌、状态、进度、功能牌栏、牌组计数、按钮亮灰。 */
    public void refresh() {
        view.renderHand(session.hand(), selected);
        view.renderStatus(session.targetScore(), session.remainingPlays(),
                session.remainingDiscards(), session.player().coins());
        view.renderProgress(session.currentLevel(), session.levelScore());
        view.renderDeckCount(session.remainingDeck().size());
        view.renderSpecialCards(session.player().specialCards());
        refreshPreview();
        view.setActionEnabled(evaluator.isPlayable(selected) && session.remainingPlays() > 0,
                !selected.isEmpty() && session.remainingDiscards() > 0);
    }

    /**
     * 刷新计分预览：选中牌可出时，左侧显示该牌型的初始分数与初始倍数
     * （手牌逐张触发后在动画中逐步增长），否则清空预览。
     */
    private void refreshPreview() {
        var eval = evaluate(selected);
        if (eval.isEmpty()) {
            view.renderPreview(null, null, null);
            return;
        }
        view.renderPreview(eval.get().type().displayName(),
                eval.get().type().baseScore(), eval.get().type().baseMultiplier());
    }

    /** 检查对局结局：过关发奖励并交给回调（进商店），终局交给回调（结算）。 */
    private void checkOutcome() {
        var outcome = session.outcome();
        switch (outcome) {
            case LEVEL_CLEARED -> {
                var reward = session.claimLevelClearReward();
                view.showLevelClearReward(session.levelScore(), reward);
                view.clearProcess();
                outcomeHandler.accept(outcome);
            }
            case VICTORY, FAILED -> {
                view.clearProcess();
                outcomeHandler.accept(outcome);
            }
            default -> { }
        }
    }
}
