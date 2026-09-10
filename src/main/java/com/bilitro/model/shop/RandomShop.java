package com.bilitro.model.shop;

import com.bilitro.model.player.Player;
import com.bilitro.model.player.SpecialCard;
import com.bilitro.model.player.SpecialCardCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认商店实现：进入商店或刷新时从功能牌配置池随机抽取商品。
 * 购买校验顺序：栏位满 → 货币不足 → 成功（对应需求 2.2.1 验收标准）。
 * TODO: 刷新是否花费代币待小组确认，当前免费。
 */
public class RandomShop implements Shop {

    /** 每批商品数量。 */
    private static final int GOODS_COUNT = 3;

    private final Player player;
    private final List<SpecialCard> pool;
    private final List<SpecialCard> goods = new ArrayList<>();

    /** 创建商店：从配置表加载功能牌池并生成首批商品。 */
    public RandomShop(Player player) {
        this.player = player;
        this.pool = new SpecialCardCatalog().loadAll();
        refresh();
    }

    /** 返回当前商品列表。 */
    @Override
    public List<SpecialCard> goods() {
        return List.copyOf(goods);
    }

    /** 随机刷新一批商品（当前免费）。 */
    @Override
    public void refresh() {
        goods.clear();
        List<SpecialCard> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        goods.addAll(shuffled.stream().limit(GOODS_COUNT).toList());
    }

    /** 购买：依次校验栏位上限与货币，成功后扣款、进货、下架该商品。 */
    @Override
    public BuyResult buy(SpecialCard item) {
        if (player.specialCards().size() >= com.bilitro.model.GameConfig.SPECIAL_CARD_LIMIT) {
            return BuyResult.SLOTS_FULL;
        }
        if (player.coins() < item.price()) {
            return BuyResult.NOT_ENOUGH_COINS;
        }
        player.addCoins(-item.price());
        player.addSpecialCard(item);
        goods.remove(item);
        return BuyResult.SUCCESS;
    }
}
