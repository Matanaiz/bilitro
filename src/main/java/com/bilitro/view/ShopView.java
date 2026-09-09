package com.bilitro.view;

import com.bilitro.model.player.SpecialCard;
import java.util.List;

/** 商店视图。 */
public interface ShopView {

    void renderGoods(List<SpecialCard> goods, int coins);

    /** 购买失败提示："栏位已满" / "货币不足"。 */
    void showBuyFailure(String message);
}
