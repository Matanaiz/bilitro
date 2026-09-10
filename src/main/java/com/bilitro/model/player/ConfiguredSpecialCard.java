package com.bilitro.model.player;

import com.bilitro.model.card.Card;
import com.bilitro.model.card.Rank;
import com.bilitro.model.card.Suit;
import com.bilitro.model.hand.HandType;
import com.bilitro.model.hand.ScoringContext;

/**
 * 配置表驱动的功能牌（答复 11：效果以配置表存储）。
 * 数据来自 resources/config/special-cards.json。
 * 效果 = 触发条件 + 效果类型 + 数值，均在配置表中声明。
 */
public class ConfiguredSpecialCard implements SpecialCard {

    /** 效果类型。 */
    public enum EffectType {
        ADD_CHIPS,              // 积分 += value
        ADD_MULT,               // 倍数 += value
        MULTIPLY_MULT,          // 倍数 *= value
        ADD_MULT_PER_SPECIAL    // 倍数 += value × 持有功能牌数量（如"抽象小丑"）
    }

    /** 触发条件。 */
    public enum ConditionType {
        ALWAYS,          // 每张计分手牌都触发
        SCORING_SUIT,    // 当前计分手牌为 conditionValue 指定花色时触发
        HAND_TYPE,       // 牌型"包含"conditionValue 指定牌型时触发（整手一次）
        SCORING_FACE,    // 当前计分手牌为人头牌（J/Q/K）时触发
        SCORING_RANKS,   // 当前计分手牌点数在 conditionValue 列表中（逗号分隔，如 ACE,TWO）时触发
        RETRIGGER_FACE,  // 重触发：人头牌额外计分 value 次（不在 onScore 中生效）
        RETRIGGER_RANKS, // 重触发：conditionValue 列表中的点数额外计分 value 次
        META_FACE        // 元规则：所有手牌均视为人头牌（如"幻视"）
    }

    private final String id;
    private final String name;
    private final String description;
    private final int price;
    private final String image;
    private final EffectType effectType;
    private final double effectValue;
    private final ConditionType conditionType;
    private final String conditionValue;

    /** 按配置表字段创建功能牌。 */
    public ConfiguredSpecialCard(String id, String name, String description, int price,
                                 String image, EffectType effectType, double effectValue,
                                 ConditionType conditionType, String conditionValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.conditionType = conditionType == null ? ConditionType.ALWAYS : conditionType;
        this.conditionValue = conditionValue;
    }

    /** 返回配置表中的唯一 id。 */
    @Override
    public String id() {
        return id;
    }

    /** 名称（界面展示用）。 */
    public String name() {
        return name;
    }

    /** 返回效果描述文本（查看浮层用）。 */
    @Override
    public String description() {
        return description;
    }

    /** 返回商店价格。 */
    @Override
    public int price() {
        return price;
    }

    /** 图片资源路径（view 层加载用）。 */
    public String image() {
        return image;
    }

    /** 计分时触发：先判断触发条件，满足则按效果类型修改积分或倍数。 */
    @Override
    public void onScore(ScoringContext ctx) {
        if (effectType == null || conditionType == null
                || conditionType == ConditionType.RETRIGGER_FACE
                || conditionType == ConditionType.RETRIGGER_RANKS
                || conditionType == ConditionType.META_FACE
                || !conditionMet(ctx)) {
            return;
        }
        switch (effectType) {
            case ADD_CHIPS -> ctx.addChips((int) effectValue);
            case ADD_MULT -> ctx.addMult((int) effectValue);
            case MULTIPLY_MULT -> ctx.multiplyMult(effectValue);
            case ADD_MULT_PER_SPECIAL -> ctx.addMult((int) effectValue * ctx.specialCount());
        }
    }

    /** 触发时机：花色/人头/点数条件逐张触发；无条件与牌型条件整手结算时触发一次。 */
    @Override
    public boolean triggerPerCard() {
        return conditionType == ConditionType.SCORING_SUIT
                || conditionType == ConditionType.SCORING_FACE
                || conditionType == ConditionType.SCORING_RANKS;
    }

    /** 重触发：人头牌条件或点数集合条件满足时，额外计分 effectValue 次。 */
    @Override
    public int retriggerCount(Card card, boolean allFaceCards) {
        if (card == null) {
            return 0;
        }
        return switch (conditionType) {
            case RETRIGGER_FACE -> isFace(card, allFaceCards) ? (int) effectValue : 0;
            case RETRIGGER_RANKS -> rankInSet(card.rank()) ? (int) effectValue : 0;
            default -> 0;
        };
    }

    /** 元规则：META_FACE 类型表示持有期间所有手牌均视为人头牌。 */
    @Override
    public boolean allCardsFace() {
        return conditionType == ConditionType.META_FACE;
    }

    /** 判断当前计分步骤是否满足触发条件。 */
    private boolean conditionMet(ScoringContext ctx) {
        return switch (conditionType) {
            case ALWAYS -> true;
            case SCORING_SUIT -> ctx.currentCard() != null
                    && ctx.currentCard().suit() == Suit.valueOf(conditionValue);
            case HAND_TYPE -> handTypeContains(ctx.handType());
            case SCORING_FACE -> ctx.currentCard() != null
                    && isFace(ctx.currentCard(), ctx.allFaceCards());
            case SCORING_RANKS -> ctx.currentCard() != null
                    && rankInSet(ctx.currentCard().rank());
            case RETRIGGER_FACE, RETRIGGER_RANKS, META_FACE -> false;
        };
    }

    /** 判断是否人头牌（J/Q/K）；allFaceCards 为 true 时所有牌均视为人头牌。 */
    private boolean isFace(Card card, boolean allFaceCards) {
        if (allFaceCards) {
            return true;
        }
        return switch (card.rank()) {
            case JACK, QUEEN, KING -> true;
            default -> false;
        };
    }

    /** 判断点数是否在 conditionValue 的逗号分隔列表中（如 "ACE,TWO,THREE,FIVE,EIGHT"）。 */
    private boolean rankInSet(Rank rank) {
        if (conditionValue == null) {
            return false;
        }
        for (String name : conditionValue.split(",")) {
            if (rank.name().equals(name.trim())) {
                return true;
            }
        }
        return false;
    }

    /** "包含某牌型"的判定：例如四条包含三条、两对包含对子、同花顺包含顺子。 */
    private boolean handTypeContains(HandType actual) {
        if (actual == null || conditionValue == null) {
            return false;
        }
        HandType wanted = HandType.valueOf(conditionValue);
        return switch (wanted) {
            case PAIR -> switch (actual) {
                case PAIR, TWO_PAIR, THREE_OF_A_KIND, FULL_HOUSE, FOUR_OF_A_KIND -> true;
                default -> false;
            };
            case THREE_OF_A_KIND -> actual == HandType.THREE_OF_A_KIND
                    || actual == HandType.FULL_HOUSE || actual == HandType.FOUR_OF_A_KIND;
            case STRAIGHT -> actual == HandType.STRAIGHT || actual == HandType.STRAIGHT_FLUSH;
            case FLUSH -> actual == HandType.FLUSH || actual == HandType.STRAIGHT_FLUSH;
            default -> actual == wanted;
        };
    }
}
