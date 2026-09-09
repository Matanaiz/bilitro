package com.bilitro.model.hand;

import com.bilitro.model.player.SpecialCard;

import java.util.List;

/**
 * 默认计分器，严格对应计分流程图：
 * 1. 以牌型基础分 / 基础倍数初始化；
 * 2. 在牌型对应手牌中从左往右，逐张累加手牌点数；
 * 3. 依次触发功能牌（一次出牌触发一轮，见类尾注）；
 * 4. 得分 = 最终分数 × 最终倍数。
 *
 * 注：流程图中功能牌触发画在逐张循环内，若按字面实现每张牌都触发一轮，
 * 功能牌效果会被放大 5 倍；此处按"每次出牌触发一轮"实现，待小组确认。
 */
public class DefaultScoreCalculator implements ScoreCalculator {

    @Override
    public ScoreBreakdown score(Evaluation eval, List<SpecialCard> specials) {
        HandType type = eval.type();
        ScoringContext ctx = new ScoringContext(type.baseScore(), type.baseMultiplier());

        int cardChips = 0;
        for (var card : eval.scoringCards()) {
            int chips = card.rank().chips();
            cardChips += chips;
            ctx.addChips(chips);
        }

        int chipsBeforeSpecials = ctx.chips();
        for (SpecialCard special : specials) {
            special.onScore(ctx);
        }

        return new ScoreBreakdown(
                type.baseScore(),
                cardChips,
                ctx.chips() - chipsBeforeSpecials,
                type.baseMultiplier(),
                ctx.mult(),
                ctx.finalScore());
    }
}
