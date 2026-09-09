package com.bilitro.model.shop;

import com.bilitro.model.player.SpecialCard;
import java.util.List;

/**
 * 商店（需求 2.2.1 / 2.2.2）。
 */
public interface Shop {

    /** 当前商品列表（进入商店时从功能牌池随机生成）。 */
    List<SpecialCard> goods();

    /** 随机刷新商品。TODO: 刷新是否花费货币待确认。 */
    void refresh();

    /**
     * 购买：依次校验栏位上限 → 货币。
     * @return 购买结果（成功 / 栏位已满 / 货币不足）
     */
    BuyResult buy(SpecialCard item);

    enum BuyResult { SUCCESS, SLOTS_FULL, NOT_ENOUGH_COINS }
}
