package com.bilitro.model.shop;

import com.bilitro.model.player.SpecialCard;
import java.util.List;

/**
 * 商店（需求 2.2.1 / 2.2.2）。
 */
public interface Shop {

    /** 当前商品列表（进入商店时从功能牌池随机生成）。 */
    List<SpecialCard> goods();

    /** 当前刷新费用（初始 2 代币，每刷新一次 +3，每次进商店重置）。 */
    int refreshCost();

    /** 随机刷新商品；代币不足时返回 false 且不刷新。 */
    boolean refresh();

    /** 出售价 = 购买价的一半。 */
    default int sellPrice(SpecialCard item) {
        return item.price() / 2;
    }

    /** 出售持有的功能牌：从玩家栏位移除并按半价返还代币。 */
    void sell(SpecialCard item);

    /**
     * 购买：依次校验栏位上限 → 货币。
     * @return 购买结果（成功 / 栏位已满 / 货币不足）
     */
    BuyResult buy(SpecialCard item);

    enum BuyResult { SUCCESS, SLOTS_FULL, NOT_ENOUGH_COINS }
}
