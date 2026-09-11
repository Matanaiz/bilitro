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
        SCORING_SUIT,    // 当前计分手牌为 conditionValue 指定花色时触发（"RANDOM" 表示随机花色）
        HAND_TYPE,       // 牌型"包含"conditionValue 指定牌型时触发（整手一次）
        SCORING_FACE,    // 当前计分手牌为人头牌（J/Q/K）时触发
        SCORING_RANKS,   // 当前计分手牌点数在 conditionValue 列表中（逗号分隔，如 ACE,TWO）时触发
        RETRIGGER_FACE,  // 重触发：人头牌额外计分 value 次（不在 onScore 中生效）
        RETRIGGER_RANKS, // 重触发：conditionValue 列表中的点数额外计分 value 次
        META_FACE,       // 元规则：所有手牌均视为人头牌（如"幻视"）
        META_ALL_SCORE,  // 元规则：所有打出的牌都参与计分（如"飞溅"）
        META_MERGE_SUITS,// 元规则：红桃=方块、梅花=黑桃（如"模糊小丑"）
        HAND_ALL_SUITS   // 打出的牌包含全部四种花色时触发（整手一次，如"花盆"）
    }

    /** 成长时机（积累类功能牌）。 */
    public enum GrowthType {
        NONE,       // 不成长
        PER_HAND,   // 每打出一次牌积累一次 growthValue
        PER_DISCARD // 每弃掉一张指定花色的牌积累一次 growthValue（如"城堡"）
    }

    /** 随机花色的变化方式（conditionValue 为 "RANDOM" 时生效）。 */
    public enum SuitMode {
        NONE,      // 不使用随机花色
        PERMANENT, // 首次随机后永久固定（如"古老小丑"）
        PER_LEVEL  // 每关开始时重新随机（如"城堡"）
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
    private final GrowthType growthType;
    private final double growthValue;
    private final SuitMode suitMode;
    private final int discardsDelta;
    private final int handSizeDelta;
    private final int creditLimit;
    private final int levelClearCoins;
    /** 已积累的成长次数（游玩中增长，效果数值随其增大）。 */
    private int growthStacks;
    /** 随机花色牌当前生效的花色（PERMANENT 首抽后固定，PER_LEVEL 每关重抽）。 */
    private Suit currentSuit;

    /** 按配置表字段创建功能牌（无成长、无被动）。 */
    public ConfiguredSpecialCard(String id, String name, String description, int price,
                                 String image, EffectType effectType, double effectValue,
                                 ConditionType conditionType, String conditionValue) {
        this(id, name, description, price, image, effectType, effectValue,
                conditionType, conditionValue, GrowthType.NONE, 0);
    }

    /** 按配置表字段创建功能牌（含成长字段，无被动）。 */
    public ConfiguredSpecialCard(String id, String name, String description, int price,
                                 String image, EffectType effectType, double effectValue,
                                 ConditionType conditionType, String conditionValue,
                                 GrowthType growthType, double growthValue) {
        this(id, name, description, price, image, effectType, effectValue,
                conditionType, conditionValue, growthType, growthValue,
                SuitMode.NONE, 0, 0, 0, 0);
    }

    /** 按配置表字段创建功能牌（全字段）。 */
    public ConfiguredSpecialCard(String id, String name, String description, int price,
                                 String image, EffectType effectType, double effectValue,
                                 ConditionType conditionType, String conditionValue,
                                 GrowthType growthType, double growthValue, SuitMode suitMode,
                                 int discardsDelta, int handSizeDelta, int creditLimit,
                                 int levelClearCoins) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.conditionType = conditionType == null ? ConditionType.ALWAYS : conditionType;
        this.conditionValue = conditionValue;
        this.growthType = growthType == null ? GrowthType.NONE : growthType;
        this.growthValue = growthValue;
        this.suitMode = suitMode == null ? SuitMode.NONE : suitMode;
        this.discardsDelta = discardsDelta;
        this.handSizeDelta = handSizeDelta;
        this.creditLimit = creditLimit;
        this.levelClearCoins = levelClearCoins;
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

    /** 返回效果描述文本（查看浮层用）；随机花色牌显示当前花色，成长类牌附上已积累的加成。 */
    @Override
    public String description() {
        String text = description;
        if (suitMode != SuitMode.NONE) {
            text += "（当前花色：" + suitText(rolledSuit()) + "）";
        }
        if (growthType != GrowthType.NONE && growthStacks > 0) {
            text += "（已积累 +" + (int) (growthStacks * growthValue) + "）";
        }
        return text;
    }

    /** 花色的中文显示名。 */
    private static String suitText(Suit suit) {
        return switch (suit) {
            case SPADE -> "黑桃";
            case HEART -> "红桃";
            case CLUB -> "梅花";
            case DIAMOND -> "方块";
        };
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
                || conditionType == ConditionType.META_ALL_SCORE
                || conditionType == ConditionType.META_MERGE_SUITS
                || !conditionMet(ctx)) {
            return;
        }
        switch (effectType) {
            case ADD_CHIPS -> ctx.addChips((int) effectiveValue());
            case ADD_MULT -> ctx.addMult((int) effectiveValue());
            case MULTIPLY_MULT -> ctx.multiplyMult(effectiveValue());
            case ADD_MULT_PER_SPECIAL -> ctx.addMult((int) effectiveValue() * ctx.specialCount());
        }
    }

    /** 一次出牌计分完成后积累成长：PER_HAND 表示每打出一次牌积累一次。 */
    @Override
    public void onHandPlayed(HandType handType) {
        if (growthType == GrowthType.PER_HAND) {
            growthStacks++;
        }
    }

    /** 弃牌成长：PER_DISCARD 表示每弃掉一张当前花色（随机花色时）的牌积累一次。 */
    @Override
    public void onDiscard(java.util.List<Card> discarded) {
        if (growthType != GrowthType.PER_DISCARD) {
            return;
        }
        for (Card card : discarded) {
            if (suitMode != SuitMode.NONE) {
                if (card.suit() == rolledSuit()) {
                    growthStacks++;
                }
            } else {
                growthStacks++;
            }
        }
    }

    /** 每关开始：PER_LEVEL 随机花色重新抽取；PERMANENT 仅在未抽过时抽取。 */
    @Override
    public void onLevelStart() {
        if (suitMode == SuitMode.PER_LEVEL
                || (suitMode == SuitMode.PERMANENT && currentSuit == null)) {
            currentSuit = randomSuit();
        }
    }

    /** 元规则：META_ALL_SCORE 表示所有打出的牌都参与计分（飞溅）。 */
    @Override
    public boolean allCardsScore() {
        return conditionType == ConditionType.META_ALL_SCORE;
    }

    /** 元规则：META_MERGE_SUITS 表示红桃=方块、梅花=黑桃（模糊小丑）。 */
    @Override
    public boolean mergesSuits() {
        return conditionType == ConditionType.META_MERGE_SUITS;
    }

    /** 随机花色牌返回当前生效的花色（未抽过时先抽一次）；否则返回 null。 */
    @Override
    public Suit currentSuit() {
        return suitMode != SuitMode.NONE ? rolledSuit() : null;
    }

    /** 被动：每关弃牌次数修正。 */
    @Override
    public int discardsDelta() {
        return discardsDelta;
    }

    /** 被动：手牌上限修正。 */
    @Override
    public int handSizeDelta() {
        return handSizeDelta;
    }

    /** 被动：商店可负债额度。 */
    @Override
    public int creditLimit() {
        return creditLimit;
    }

    /** 被动：每关通关时额外获得的代币。 */
    @Override
    public int levelClearCoins() {
        return levelClearCoins;
    }

    /** 返回当前生效的随机花色（未抽取时先抽一次）。 */
    private Suit rolledSuit() {
        if (currentSuit == null) {
            currentSuit = randomSuit();
        }
        return currentSuit;
    }

    /** 随机抽一个花色。 */
    private static Suit randomSuit() {
        Suit[] suits = Suit.values();
        return suits[new java.util.Random().nextInt(suits.length)];
    }

    /** 返回当前已积累的成长次数（存档与测试用）。 */
    public int growthStacks() {
        return growthStacks;
    }

    /** 效果数值 = 基础值 + 已积累次数 × 每次积累量。 */
    private double effectiveValue() {
        return effectValue + growthStacks * growthValue;
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
                    && suitMatches(ctx.currentCard().suit(), resolveSuit(), ctx.mergeSuits());
            case HAND_TYPE -> handTypeContains(ctx.handType());
            case SCORING_FACE -> ctx.currentCard() != null
                    && isFace(ctx.currentCard(), ctx.allFaceCards());
            case SCORING_RANKS -> ctx.currentCard() != null
                    && rankInSet(ctx.currentCard().rank());
            case HAND_ALL_SUITS -> ctx.playedCards() != null
                    && ctx.playedCards().stream().map(Card::suit).collect(
                            java.util.stream.Collectors.toSet()).size() == Suit.values().length;
            case RETRIGGER_FACE, RETRIGGER_RANKS, META_FACE, META_ALL_SCORE, META_MERGE_SUITS -> false;
        };
    }

    /** 解析花色条件：conditionValue 为 "RANDOM" 时使用当前随机花色，否则按枚举名解析。 */
    private Suit resolveSuit() {
        if ("RANDOM".equals(conditionValue)) {
            return rolledSuit();
        }
        return Suit.valueOf(conditionValue);
    }

    /** 花色比对：归并生效时按色系组比对（红桃=方块、梅花=黑桃），否则字面相等。 */
    private boolean suitMatches(Suit actual, Suit wanted, boolean mergeSuits) {
        if (!mergeSuits) {
            return actual == wanted;
        }
        return suitGroup(actual) == suitGroup(wanted);
    }

    /** 花色归并分组：红桃/方块同组，梅花/黑桃同组。 */
    private static int suitGroup(Suit suit) {
        return switch (suit) {
            case HEART, DIAMOND -> 0;
            case CLUB, SPADE -> 1;
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
