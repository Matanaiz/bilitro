package com.bilitro.model.hand;

import com.bilitro.model.player.SpecialCard;

import java.util.ArrayList;
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
     * 计算一次出牌的得分明细：基于分步快照汇总
     * （最终得分 = 最终分数 × 最终倍数）。
     */
    @Override
    public ScoreBreakdown score(Evaluation eval, List<SpecialCard> specials) {
        List<ScoreStep> steps = steps(eval, specials);
        int cardChips = eval.scoringCards().stream()
                .mapToInt(c -> c.rank().chips())
                .sum();
        ScoreStep last = steps.get(steps.size() - 1);
        return new ScoreBreakdown(
                eval.type().baseScore(),
                cardChips,
                last.chipsAfter() - eval.type().baseScore() - cardChips,
                eval.type().baseMultiplier(),
                last.multAfter(),
                last.chipsAfter() * last.multAfter());
    }

    /**
     * 分步计分：逐张累加点数并触发一轮功能牌，每步记录
     * （手牌, 当前积分, 当前倍数）快照，供界面播放计分过程。
     */
    @Override
    public List<ScoreStep> steps(Evaluation eval, List<SpecialCard> specials) {
        HandType type = eval.type();
        ScoringContext ctx = new ScoringContext(type.baseScore(), type.baseMultiplier(), type);

        List<ScoreStep> steps = new ArrayList<>();
        boolean first = true;
        for (var card : eval.scoringCards()) {
            ctx.addChips(card.rank().chips());
            ctx.setCurrentCard(card, first);
            // 每处理一张手牌，依次触发一轮全部功能牌
            for (SpecialCard special : specials) {
                special.onScore(ctx);
            }
            steps.add(new ScoreStep(card, ctx.chips(), ctx.mult()));
            first = false;
        }
        return steps;
    }
}
