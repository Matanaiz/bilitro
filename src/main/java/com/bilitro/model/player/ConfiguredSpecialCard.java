package com.bilitro.model.player;

import com.bilitro.model.hand.ScoringContext;

/**
 * 配置表驱动的功能牌（答复 11：效果以配置表存储）。
 * 数据来自 resources/config/special-cards.json，效果按 type 分发执行。
 */
public class ConfiguredSpecialCard implements SpecialCard {

    /** 计分类效果类型；被动类效果（改手牌数/次数）稍后扩展。 */
    public enum EffectType {
        ADD_CHIPS,      // 分数 += value
        ADD_MULT,       // 倍数 += value
        MULTIPLY_MULT   // 倍数 *= value
    }

    private final String id;
    private final String name;
    private final String description;
    private final int price;
    private final String image;
    private final EffectType effectType;
    private final double effectValue;

    public ConfiguredSpecialCard(String id, String name, String description, int price,
                                 String image, EffectType effectType, double effectValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.effectType = effectType;
        this.effectValue = effectValue;
    }

    @Override
    public String id() {
        return id;
    }

    /** 名称（界面展示用）。 */
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public int price() {
        return price;
    }

    /** 图片资源路径（view 层加载用）。 */
    public String image() {
        return image;
    }

    @Override
    public void onScore(ScoringContext ctx) {
        if (effectType == null) {
            return;
        }
        switch (effectType) {
            case ADD_CHIPS -> ctx.addChips((int) effectValue);
            case ADD_MULT -> ctx.addMult((int) effectValue);
            case MULTIPLY_MULT -> ctx.multiplyMult(effectValue);
        }
    }
}
