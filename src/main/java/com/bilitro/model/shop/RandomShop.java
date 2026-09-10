package com.bilitro.model.shop;

import com.bilitro.model.GameConfig;
import com.bilitro.model.player.Player;
import com.bilitro.model.player.SpecialCard;
import com.bilitro.model.player.SpecialCardCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认商店实现：进入商店时从功能牌配置池随机抽取商品。
 * 购买校验顺序：栏位满 → 货币不足 → 成功（对应需求 2.2.1 验收标准）。
 * 刷新收费：初始 2 代币，每刷新一次 +3，每次进商店（新建实例）重置。
 * 出售：按购买价的一半返还代币。
 */
public class RandomShop implements Shop {

    /** 每批商品数量。 */
    private static final int GOODS_COUNT = 3;

    private final Player player;
    private final List<SpecialCard> pool;
    private final List<SpecialCard> goods = new ArrayList<>();
    private int refreshCost = GameConfig.SHOP_REFRESH_BASE_COST;

    /** 创建商店：从配置表加载功能牌池并免费生成首批商品。 */
    public RandomShop(Player player) {
        this.player = player;
        this.pool = new SpecialCardCatalog().loadAll();
        rollGoods();
    }

    /** 返回当前商品列表。 */
    @Override
    public List<SpecialCard> goods() {
        return List.copyOf(goods);
    }

    /** 返回当前刷新费用。 */
    @Override
    public int refreshCost() {
        return refreshCost;
    }

    /** 付费刷新：代币不足返回 false；成功后费用递增。 */
    @Override
    public boolean refresh() {
        if (player.coins() < refreshCost) {
            return false;
        }
        player.addCoins(-refreshCost);
        refreshCost += GameConfig.SHOP_REFRESH_COST_STEP;
        rollGoods();
        return true;
    }

    /** 出售功能牌：从玩家栏位移除并按半价返还代币。 */
    @Override
    public void sell(SpecialCard item) {
        if (player.removeSpecialCard(item)) {
            player.addCoins(sellPrice(item));
        }
    }

    /** 购买：依次校验栏位上限与货币，成功后扣款、进货、下架该商品。 */
    @Override
    public BuyResult buy(SpecialCard item) {
        if (player.specialCards().size() >= GameConfig.SPECIAL_CARD_LIMIT) {
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

    /** 从功能牌池随机抽取一批商品。 */
    private void rollGoods() {
        goods.clear();
        List<SpecialCard> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled);
        goods.addAll(shuffled.stream().limit(GOODS_COUNT).toList());
    }
}
