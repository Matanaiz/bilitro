package com.bilitro.model.hand;

import com.bilitro.model.player.SpecialCard;

import java.util.List;

/**
 * 默认计分器，严格对应计分流程图：
 * 1. 以牌型基础分 / 基础倍数初始化；
 * 2. 在牌型对应手牌中从左往右，逐张累加手牌点数；
 * 3. 每处理一张手牌，就依次触发一轮全部功能牌（小组已确认：每张牌都触发一轮）；
 * 4. 得分 = 最终分数 × 最终倍数。
 */
public class DefaultScoreCalculator implements ScoreCalculator {

    /**
     * 计算一次出牌的得分：初始化基础分/倍数 → 逐张累加点数并触发一轮功能牌
     * → 汇总为计分明细（最终得分 = 最终分数 × 最终倍数）。
     */
    @Override
    public ScoreBreakdown score(Evaluation eval, List<SpecialCard> specials) {
        HandType type = eval.type();
        ScoringContext ctx = new ScoringContext(type.baseScore(), type.baseMultiplier());

        int cardChips = 0;
        for (var card : eval.scoringCards()) {
            int chips = card.rank().chips();
            cardChips += chips;
            ctx.addChips(chips);
            // 每处理一张手牌，依次触发一轮全部功能牌
            for (SpecialCard special : specials) {
                special.onScore(ctx);
            }
        }

        return new ScoreBreakdown(
                type.baseScore(),
                cardChips,
                ctx.chips() - type.baseScore() - cardChips,
                type.baseMultiplier(),
                ctx.mult(),
                ctx.finalScore());
    }
}
