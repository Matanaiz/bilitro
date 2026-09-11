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
        int cardChips = cardChipsWithRetrigger(eval, specials);
        ScoreStep last = steps.get(steps.size() - 1);
        return new ScoreBreakdown(
                eval.type().baseScore(),
                cardChips,
                last.chipsAfter() - eval.type().baseScore() - cardChips,
                eval.type().baseMultiplier(),
                last.multAfter(),
                last.chipsAfter() * last.multAfter());
    }

    /** 手牌点数合计（计入重触发：被重触发的牌点数重复累加）。 */
    private int cardChipsWithRetrigger(Evaluation eval, List<SpecialCard> specials) {
        boolean allFace = allCardsFace(specials);
        int sum = 0;
        for (var card : eval.scoringCards()) {
            sum += card.rank().chips() * (1 + retriggerCount(specials, card, allFace));
        }
        return sum;
    }

    /**
     * 分步计分：逐张累加点数并触发一轮功能牌，每步记录
     * （手牌, 当前积分, 当前倍数）快照，供界面播放计分过程。
     */
    @Override
    public List<ScoreStep> steps(Evaluation eval, List<SpecialCard> specials) {
        HandType type = eval.type();
        ScoringContext ctx = new ScoringContext(type.baseScore(), type.baseMultiplier(), type);
        boolean allFace = allCardsFace(specials);
        ctx.setAllFaceCards(allFace);
        ctx.setSpecialCount(specials.size());
        ctx.setPlayedCards(eval.playedCards());

        List<ScoreStep> steps = new ArrayList<>();
        boolean first = true;
        for (var card : eval.scoringCards()) {
            ctx.setCurrentCard(card, first);
            // 重触发：满足条件的功能牌会让这张牌额外计分若干次
            int repeats = 1 + retriggerCount(specials, card, allFace);
            for (int i = 0; i < repeats; i++) {
                ctx.addChips(card.rank().chips());
                // 每处理一张手牌，触发一轮逐张类功能牌（花色、人头、点数条件）
                for (SpecialCard special : specials) {
                    if (special.triggerPerCard()) {
                        special.onScore(ctx);
                    }
                }
                steps.add(new ScoreStep(card, ctx.chips(), ctx.mult()));
            }
            first = false;
        }
        // 整手类功能牌（无条件、牌型条件）在结算末尾触发一次
        boolean hasPerHand = specials.stream().anyMatch(s -> !s.triggerPerCard());
        if (hasPerHand) {
            ctx.setCurrentCard(null, false);
            for (SpecialCard special : specials) {
                if (!special.triggerPerCard()) {
                    special.onScore(ctx);
                }
            }
            // 追加一个无牌的结算快照，供界面显示最终积分与倍数
            steps.add(new ScoreStep(null, ctx.chips(), ctx.mult()));
        }
        return steps;
    }

    /** 是否有"所有牌均视为人头牌"的元规则功能牌（幻视）。 */
    private boolean allCardsFace(List<SpecialCard> specials) {
        return specials.stream().anyMatch(SpecialCard::allCardsFace);
    }

    /** 汇总所有功能牌对当前这张牌的重触发次数。 */
    private int retriggerCount(List<SpecialCard> specials, com.bilitro.model.card.Card card, boolean allFace) {
        int count = 0;
        for (SpecialCard special : specials) {
            count += special.retriggerCount(card, allFace);
        }
        return count;
    }
}
