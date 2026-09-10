package com.bilitro.model.player;

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
        ADD_CHIPS,      // 积分 += value
        ADD_MULT,       // 倍数 += value
        MULTIPLY_MULT   // 倍数 *= value
    }

    /** 触发条件。 */
    public enum ConditionType {
        ALWAYS,         // 每张计分手牌都触发
        SCORING_SUIT,   // 当前计分手牌为 conditionValue 指定花色时触发
        HAND_TYPE       // 牌型"包含"conditionValue 指定牌型时触发（整手一次）
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
        if (effectType == null || !conditionMet(ctx)) {
            return;
        }
        switch (effectType) {
            case ADD_CHIPS -> ctx.addChips((int) effectValue);
            case ADD_MULT -> ctx.addMult((int) effectValue);
            case MULTIPLY_MULT -> ctx.multiplyMult(effectValue);
        }
    }

    /** 判断当前计分步骤是否满足触发条件。 */
    private boolean conditionMet(ScoringContext ctx) {
        return switch (conditionType) {
            case ALWAYS -> true;
            case SCORING_SUIT -> ctx.currentCard() != null
                    && ctx.currentCard().suit() == Suit.valueOf(conditionValue);
            case HAND_TYPE -> ctx.isFirstCard() && handTypeContains(ctx.handType());
        };
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
