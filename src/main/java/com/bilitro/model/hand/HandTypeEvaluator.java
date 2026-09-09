package com.bilitro.model.hand;

import com.bilitro.model.card.Card;
import java.util.List;
import java.util.Optional;

/**
 * 牌型判定器：查表判定选中手牌构成的最高优先级牌型。
 * 对应需求 2.1.1（出牌规则校验）与 2.1.2（牌型判定）。
 */
public interface HandTypeEvaluator {

    /** 判定这组牌构成的最高牌型；不满足任何牌型时返回 empty。 */
    Optional<HandType> evaluate(List<Card> selected);

    /**
     * 完整判定：最高牌型 + 构成该牌型的手牌（从左往右排序）。
     * 计分流程按"牌型对应手牌"逐张计分，未构成牌型的牌不参与计分。
     */
    Optional<Evaluation> evaluateDetail(List<Card> selected);

    /** 选中的张数/组合是否满足出牌规则（用于出牌按钮亮起校验）。 */
    boolean isPlayable(List<Card> selected);
}
