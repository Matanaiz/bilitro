package com.bilitro.controller;

import com.bilitro.model.GameConfig;
import com.bilitro.model.card.Card;
import com.bilitro.model.card.StandardDeck;
import com.bilitro.model.game.DefaultGameSession;
import com.bilitro.model.game.GameSession;
import com.bilitro.model.game.LevelRule;
import com.bilitro.model.hand.DefaultHandTypeEvaluator;
import com.bilitro.model.hand.DefaultScoreCalculator;
import com.bilitro.model.hand.ScoreCalculator;
import com.bilitro.model.player.ConfiguredSpecialCard;
import com.bilitro.model.player.DefaultPlayer;
import com.bilitro.model.player.SpecialCard;
import com.bilitro.view.GameView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** 对局控制器测试：选牌、出牌、弃牌、预览、结局回调（用假视图代替 JavaFX 界面）。 */
class DefaultGameControllerTest {

    /** 假视图：记录每次调用，计分过程动画改为同步执行完成回调。 */
    static class FakeGameView implements GameView {
        List<Card> lastHand = List.of();
        List<Card> lastSelected = List.of();
        String previewName;
        Integer previewChips;
        Integer previewMult;
        Integer effectScore;
        String tip;
        List<Card> deckShown;
        GameSession.RewardBreakdown rewardShown;
        boolean processCleared;
        boolean canPlay = true;
        boolean canDiscard = true;

        @Override
        public void renderHand(List<Card> hand, List<Card> selected, List<Card> scoringCards) {
            lastHand = hand;
            lastSelected = selected;
        }

        @Override
        public void renderStatus(int targetScore, int plays, int discards, int coins) { }

        @Override
        public void setActionEnabled(boolean canPlay, boolean canDiscard) {
            this.canPlay = canPlay;
            this.canDiscard = canDiscard;
        }

        @Override
        public void playScoreEffect(int score) {
            effectScore = score;
        }

        @Override
        public void showSpecialCardTip(String description) {
            tip = description;
        }

        @Override
        public void showDeck(List<Card> remaining) {
            deckShown = remaining;
        }

        @Override
        public void showMessage(String message) { }

        @Override
        public void showLevelClearReward(int levelScore, GameSession.RewardBreakdown reward) {
            rewardShown = reward;
        }

        @Override
        public void renderPreview(String handTypeName, Integer chips, Integer mult) {
            previewName = handTypeName;
            previewChips = chips;
            previewMult = mult;
        }

        @Override
        public void renderProgress(int level, int totalScore) { }

        @Override
        public void renderDeckCount(int remaining) { }

        @Override
        public void renderSpecialCards(List<SpecialCard> specials) { }

        @Override
        public void clearProcess() {
            processCleared = true;
        }

        @Override
        public void playScoringProcess(List<ScoreCalculator.ScoreStep> steps, Runnable onFinished) {
            onFinished.run(); // 测试中不播动画，直接执行完成回调
        }
    }

    /** 以指定目标分搭建控制器与假视图。 */
    private DefaultGameController newController(GameSession session, FakeGameView view) {
        return new DefaultGameController(session, new DefaultHandTypeEvaluator(),
                new DefaultScoreCalculator(), view);
    }

    private GameSession newSession(DefaultPlayer player, int targetScore) {
        return new DefaultGameSession(new StandardDeck(), new DefaultHandTypeEvaluator(),
                new DefaultScoreCalculator(), player, new LevelRule(targetScore, Set.of(), "测试关"));
    }

    @Test
    void 点选手牌可切换选中与取消() {
        GameSession s = newSession(new DefaultPlayer(), 10000);
        DefaultGameController c = newController(s, new FakeGameView());
        Card card = s.hand().get(0);
        c.toggleSelect(card);
        assertEquals(List.of(card), c.selected());
        c.toggleSelect(card);
        assertTrue(c.selected().isEmpty());
    }

    @Test
    void 选中达到上限后无法加选() {
        GameSession s = newSession(new DefaultPlayer(), 10000);
        DefaultGameController c = newController(s, new FakeGameView());
        for (int i = 0; i < GameConfig.HAND_SIZE; i++) {
            c.toggleSelect(s.hand().get(i));
        }
        assertEquals(GameConfig.MAX_SELECT, c.selected().size());
    }

    @Test
    void 选中后预览显示牌型初始分数与倍数() {
        GameSession s = newSession(new DefaultPlayer(), 10000);
        FakeGameView view = new FakeGameView();
        DefaultGameController c = newController(s, view);
        c.toggleSelect(s.hand().get(0)); // 单张为高牌
        assertEquals("高牌", view.previewName);
        assertNotNull(view.previewChips);
        assertNotNull(view.previewMult);
    }

    @Test
    void 取消选中后预览清空() {
        GameSession s = newSession(new DefaultPlayer(), 10000);
        FakeGameView view = new FakeGameView();
        DefaultGameController c = newController(s, view);
        Card card = s.hand().get(0);
        c.toggleSelect(card);
        c.toggleSelect(card);
        assertNull(view.previewName);
    }

    @Test
    void 出牌扣次数并清空选中且播放得分特效() {
        GameSession s = newSession(new DefaultPlayer(), 10000);
        FakeGameView view = new FakeGameView();
        DefaultGameController c = newController(s, view);
        c.toggleSelect(s.hand().get(0));
        c.onPlay();
        assertEquals(GameConfig.PLAYS_PER_LEVEL - 1, s.remainingPlays());
        assertTrue(c.selected().isEmpty());
        assertNotNull(view.effectScore);
        assertTrue(view.effectScore > 0);
    }

    @Test
    void 未选牌时出牌无响应() {
        GameSession s = newSession(new DefaultPlayer(), 10000);
        DefaultGameController c = newController(s, new FakeGameView());
        c.onPlay();
        assertEquals(GameConfig.PLAYS_PER_LEVEL, s.remainingPlays());
    }

    @Test
    void 弃牌扣次数并清空选中() {
        GameSession s = newSession(new DefaultPlayer(), 10000);
        DefaultGameController c = newController(s, new FakeGameView());
        c.toggleSelect(s.hand().get(0));
        c.toggleSelect(s.hand().get(1));
        c.onDiscard();
        assertEquals(GameConfig.DISCARDS_PER_LEVEL - 1, s.remainingDiscards());
        assertTrue(c.selected().isEmpty());
    }

    @Test
    void 弃牌次数耗尽后弃牌无响应() {
        GameSession s = newSession(new DefaultPlayer(), 10000);
        DefaultGameController c = newController(s, new FakeGameView());
        for (int i = 0; i < GameConfig.DISCARDS_PER_LEVEL; i++) {
            c.toggleSelect(s.hand().get(0));
            c.onDiscard();
        }
        assertEquals(0, s.remainingDiscards());
        c.toggleSelect(s.hand().get(0));
        c.onDiscard(); // 无次数，应直接返回且不清空选中
        assertEquals(1, c.selected().size());
    }

    @Test
    void 通关后弹出奖励并触发结局回调() {
        GameSession s = newSession(new DefaultPlayer(), 1); // 目标分极低，任意出牌即过关
        FakeGameView view = new FakeGameView();
        DefaultGameController c = newController(s, view);
        AtomicReference<GameSession.RoundOutcome> outcome = new AtomicReference<>();
        c.setOutcomeHandler(outcome::set);
        c.toggleSelect(s.hand().get(0));
        c.onPlay();
        assertEquals(GameSession.RoundOutcome.LEVEL_CLEARED, outcome.get());
        assertNotNull(view.rewardShown);
        assertTrue(view.processCleared);
    }

    @Test
    void 失败时触发结局回调() {
        GameSession s = newSession(new DefaultPlayer(), Integer.MAX_VALUE);
        FakeGameView view = new FakeGameView();
        DefaultGameController c = newController(s, view);
        AtomicReference<GameSession.RoundOutcome> outcome = new AtomicReference<>();
        c.setOutcomeHandler(outcome::set);
        for (int i = 0; i < GameConfig.PLAYS_PER_LEVEL; i++) {
            c.toggleSelect(s.hand().get(0));
            c.onPlay();
        }
        assertEquals(GameSession.RoundOutcome.FAILED, outcome.get());
    }

    @Test
    void 查看牌组弹出剩余牌() {
        GameSession s = newSession(new DefaultPlayer(), 10000);
        FakeGameView view = new FakeGameView();
        DefaultGameController c = newController(s, view);
        c.onViewDeck();
        assertEquals(s.remainingDeck(), view.deckShown);
    }

    @Test
    void 点击功能牌弹出效果说明() {
        DefaultPlayer player = new DefaultPlayer();
        player.addSpecialCard(new ConfiguredSpecialCard("joker", "小丑", "测试效果文本", 2, "",
                ConfiguredSpecialCard.EffectType.ADD_MULT, 4,
                ConfiguredSpecialCard.ConditionType.ALWAYS, null));
        GameSession s = newSession(player, 10000);
        FakeGameView view = new FakeGameView();
        DefaultGameController c = newController(s, view);
        c.onInspectSpecialCard("joker");
        assertEquals("测试效果文本", view.tip);
    }
}
