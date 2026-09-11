package com.bilitro.model.game;

import com.bilitro.model.GameConfig;
import com.bilitro.model.card.Deck;
import com.bilitro.model.card.StandardDeck;
import com.bilitro.model.hand.DefaultHandTypeEvaluator;
import com.bilitro.model.hand.DefaultScoreCalculator;
import com.bilitro.model.player.DefaultPlayer;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** 对局核心测试：开局发牌、出牌、弃牌、结束判定、通关奖励。 */
class DefaultGameSessionTest {

    /** 以指定目标分创建一局（无禁用花色）。 */
    private GameSession newSession(int targetScore) {
        Deck deck = new StandardDeck();
        return new DefaultGameSession(deck, new DefaultHandTypeEvaluator(),
                new DefaultScoreCalculator(), new DefaultPlayer(),
                new LevelRule(targetScore, Set.of(), "测试关"));
    }

    @Test
    void 开局发满手牌且次数正确() {
        GameSession s = newSession(100);
        assertEquals(GameConfig.HAND_SIZE, s.hand().size());
        assertEquals(GameConfig.PLAYS_PER_LEVEL, s.remainingPlays());
        assertEquals(GameConfig.DISCARDS_PER_LEVEL, s.remainingDiscards());
        assertEquals(1, s.currentLevel());
        assertEquals(GameSession.RoundOutcome.ONGOING, s.outcome());
    }

    @Test
    void 出牌扣次数并补牌() {
        GameSession s = newSession(10000);
        int before = s.remainingPlays();
        s.play(s.hand().subList(0, 1));
        assertEquals(before - 1, s.remainingPlays());
        assertEquals(GameConfig.HAND_SIZE, s.hand().size());
        assertTrue(s.levelScore() > 0);
    }

    @Test
    void 弃牌扣次数并补牌() {
        GameSession s = newSession(10000);
        int before = s.remainingDiscards();
        s.discard(s.hand().subList(0, 2));
        assertEquals(before - 1, s.remainingDiscards());
        assertEquals(GameConfig.HAND_SIZE, s.hand().size());
    }

    @Test
    void 达标即过关() {
        GameSession s = newSession(1); // 目标分极低，任意出牌即达标
        s.play(s.hand().subList(0, 1));
        assertEquals(GameSession.RoundOutcome.LEVEL_CLEARED, s.outcome());
    }

    @Test
    void 第八关达标为整局胜利() {
        GameSession s = newSession(1);
        for (int i = 1; i < GameConfig.MAX_LEVEL; i++) {
            s.play(s.hand().subList(0, 1));
            assertEquals(GameSession.RoundOutcome.LEVEL_CLEARED, s.outcome());
            s.advanceLevel(new LevelRule(1, Set.of(), "下一关"));
        }
        assertEquals(GameConfig.MAX_LEVEL, s.currentLevel());
        s.play(s.hand().subList(0, 1));
        assertEquals(GameSession.RoundOutcome.VICTORY, s.outcome());
    }

    @Test
    void 次数耗尽未达标判失败() {
        GameSession s = newSession(Integer.MAX_VALUE);
        for (int i = 0; i < GameConfig.PLAYS_PER_LEVEL; i++) {
            s.play(s.hand().subList(0, 1));
        }
        assertEquals(GameSession.RoundOutcome.FAILED, s.outcome());
    }

    @Test
    void 通关奖励含固定奖励剩余出牌和利息() {
        DefaultPlayer player = new DefaultPlayer(10); // 10 代币 → 利息 2
        GameSession s = new DefaultGameSession(new StandardDeck(),
                new DefaultHandTypeEvaluator(), new DefaultScoreCalculator(),
                player, new LevelRule(1, Set.of(), "测试关"));
        s.play(s.hand().subList(0, 1)); // 用掉 1 次出牌，剩 3 次
        var reward = s.claimLevelClearReward();
        // 4 固定 + 3 剩余出牌 + 2 利息 = 9
        assertEquals(4, reward.base());
        assertEquals(3, reward.playBonus());
        assertEquals(2, reward.interest());
        assertEquals(9, reward.total());
        assertEquals(19, player.coins());
    }

    @Test
    void 次数为零时再出牌抛异常() {
        GameSession s = newSession(Integer.MAX_VALUE);
        for (int i = 0; i < GameConfig.PLAYS_PER_LEVEL; i++) {
            s.play(s.hand().subList(0, 1));
        }
        assertThrows(IllegalStateException.class, () -> s.play(s.hand().subList(0, 1)));
    }
}
