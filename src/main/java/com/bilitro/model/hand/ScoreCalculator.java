package com.bilitro.model.hand;

import com.bilitro.model.player.SpecialCard;
import java.util.List;

/**
 * 计分器：按计分流程图执行——
 * 牌型基础分/倍数初始化 → 逐张手牌累加点数并触发功能牌 → 得分 = 最终分数 × 最终倍数。
 * 对应需求 2.1.2 自动计分。
 */
public interface ScoreCalculator {

    /**
     * 计算一次出牌的得分。
     * @param eval     牌型判定结果（牌型 + 参与计分的手牌，从左往右）
     * @param specials 当前持有的功能牌（逐张手牌计分时依次触发）
     * @return 计分明细（含过程值，供 view 做逐张跳动/特效）
     */
    ScoreBreakdown score(Evaluation eval, List<SpecialCard> specials);

    /**
     * 分步计分：每处理一张计分手牌（含该轮功能牌触发）后记录一个快照，
     * 供界面从左到右逐张播放计分过程，当前积分与倍数随步骤增长。
     */
    List<ScoreStep> steps(Evaluation eval, List<SpecialCard> specials);

    /**
     * 计分步骤快照。
     * @param card       本步处理的手牌；为 null 表示整手功能牌结算快照（末步）
     * @param chipsAfter 本步后的当前积分
     * @param multAfter  本步后的当前倍数
     */
    record ScoreStep(com.bilitro.model.card.Card card, int chipsAfter, int multAfter) {
    }

    /**
     * 计分明细。
     * @param baseChips   牌型基础分
     * @param cardChips   手牌点数累计
     * @param bonusChips  功能牌加成分
     * @param baseMult    牌型基础倍数
     * @param finalMult   最终倍数（含功能牌加/乘算）
     * @param finalScore  最终得分 = (baseChips+cardChips+bonusChips) × finalMult
     */
    record ScoreBreakdown(int baseChips, int cardChips, int bonusChips,
                          int baseMult, int finalMult, int finalScore) {
    }
}
