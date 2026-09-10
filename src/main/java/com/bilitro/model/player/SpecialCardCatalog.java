package com.bilitro.model.player;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 功能牌配置表加载器：从 classpath 的 config/special-cards.json 读取功能牌池。
 * 配置表当前为空占位，稍后补充牌图片与效果文本（字段与 JSON 一一对应）。
 */
public class SpecialCardCatalog {

    private static final String CONFIG_PATH = "/config/special-cards.json";

    /** 配置表条目，字段与 JSON 对应。 */
    private static class Entry {
        String id;
        String name;
        String description;
        int price;
        String image;
        @SerializedName("effectType")
        ConfiguredSpecialCard.EffectType effectType;
        @SerializedName("effectValue")
        double effectValue;
        @SerializedName("conditionType")
        ConfiguredSpecialCard.ConditionType conditionType;
        @SerializedName("conditionValue")
        String conditionValue;
        @SerializedName("growthType")
        ConfiguredSpecialCard.GrowthType growthType;
        @SerializedName("growthValue")
        double growthValue;
    }

    /** 加载全部功能牌；配置表为空时返回空列表。 */
    public List<SpecialCard> loadAll() {
        try (InputStream in = SpecialCardCatalog.class.getResourceAsStream(CONFIG_PATH)) {
            if (in == null) {
                return List.of();
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                Entry[] entries = new Gson().fromJson(reader, Entry[].class);
                if (entries == null) {
                    return List.of();
                }
                return java.util.Arrays.stream(entries)
                        .<SpecialCard>map(e -> new ConfiguredSpecialCard(
                                e.id, e.name, e.description, e.price,
                                e.image, e.effectType, e.effectValue,
                                e.conditionType, e.conditionValue,
                                e.growthType, e.growthValue))
                        .toList();
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取功能牌配置表失败: " + CONFIG_PATH, e);
        }
    }
}
