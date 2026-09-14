package com.bilitro.controller;

import com.bilitro.model.player.Player;
import com.bilitro.model.player.SpecialCard;
import com.bilitro.model.shop.Shop;
import com.bilitro.view.ShopView;

/** 默认商店控制器：接收商店界面事件，调用 Shop，按购买结果提示。 */
public class DefaultShopController implements ShopController {

    private final Shop shop;
    private final Player player;
    private final ShopView view;

    /** 离开商店回调（进入下一关），由 App 注入。 */
    private Runnable leaveHandler = () -> { };

    /** 创建商店控制器并刷新一次界面。 */
    public DefaultShopController(Shop shop, Player player, ShopView view) {
        this.shop = shop;
        this.player = player;
        this.view = view;
    }

    /** 注入离开商店回调。 */
    public void setLeaveHandler(Runnable handler) {
        this.leaveHandler = handler;
    }

    /** 购买商品：成功或按原因弹提示，然后刷新界面。 */
    @Override
    public void onBuy(SpecialCard item) {
        Shop.BuyResult result = shop.buy(item);
        switch (result) {
            case SLOTS_FULL -> view.showBuyFailure("功能牌栏位已满");
            case NOT_ENOUGH_COINS -> view.showBuyFailure("代币不足");
            case SUCCESS -> { }
        }
        refresh();
    }

    /** 刷新商品：代币不足时提示，成功后费用递增。 */
    @Override
    public void onRefresh() {
        if (!shop.refresh()) {
            view.showBuyFailure("代币不足，无法刷新");
        }
        refresh();
    }

    /** 出售功能牌：半价返还代币。 */
    @Override
    public void onSell(SpecialCard item) {
        shop.sell(item);
        refresh();
    }

    /** 离开商店，进入下一关。 */
    @Override
    public void onLeave() {
        leaveHandler.run();
    }

    /** 全量刷新商店界面。 */
    public void refresh() {
        view.renderGoods(shop.goods(), player.coins());
        view.renderOwned(player.specialCards());
        view.renderRefreshCost(shop.refreshCost());
    }
}
