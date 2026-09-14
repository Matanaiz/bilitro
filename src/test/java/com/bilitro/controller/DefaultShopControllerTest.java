package com.bilitro.controller;

import com.bilitro.model.player.ConfiguredSpecialCard;
import com.bilitro.model.player.DefaultPlayer;
import com.bilitro.model.player.SpecialCard;
import com.bilitro.model.shop.RandomShop;
import com.bilitro.view.ShopView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/** 商店控制器测试：购买提示、刷新提示、出售刷新界面、离开回调（用假视图代替 JavaFX 界面）。 */
class DefaultShopControllerTest {

    /** 假商店视图：记录错误提示与最近一次刷新的内容。 */
    static class FakeShopView implements ShopView {
        String lastError;
        List<SpecialCard> lastGoods = List.of();
        List<SpecialCard> lastOwned = List.of();
        int lastRefreshCost = -1;

        @Override
        public void renderGoods(List<SpecialCard> goods, int coins) {
            lastGoods = goods;
        }

        @Override
        public void renderOwned(List<SpecialCard> owned) {
            lastOwned = owned;
        }

        @Override
        public void renderRefreshCost(int cost) {
            lastRefreshCost = cost;
        }

        @Override
        public void showBuyFailure(String message) {
            lastError = message;
        }
    }

    /** 构造一张指定 id 与价格的测试功能牌。 */
    private static SpecialCard filler(String id, int price) {
        return new ConfiguredSpecialCard(id, id, id, price, "",
                ConfiguredSpecialCard.EffectType.ADD_CHIPS, 0,
                ConfiguredSpecialCard.ConditionType.ALWAYS, null);
    }

    @Test
    void 购买成功不弹错误提示且界面刷新() {
        DefaultPlayer player = new DefaultPlayer(1000);
        RandomShop shop = new RandomShop(player);
        FakeShopView view = new FakeShopView();
        DefaultShopController c = new DefaultShopController(shop, player, view);
        SpecialCard item = shop.goods().get(0);
        c.onBuy(item);
        assertNull(view.lastError);
        assertTrue(view.lastOwned.stream().anyMatch(s -> s.id().equals(item.id())));
        assertEquals(shop.refreshCost(), view.lastRefreshCost);
    }

    @Test
    void 代币不足购买弹提示() {
        DefaultPlayer player = new DefaultPlayer(0);
        RandomShop shop = new RandomShop(player);
        FakeShopView view = new FakeShopView();
        DefaultShopController c = new DefaultShopController(shop, player, view);
        SpecialCard item = shop.goods().stream()
                .filter(g -> g.price() > 0).findFirst().orElseThrow();
        c.onBuy(item);
        assertEquals("代币不足", view.lastError);
    }

    @Test
    void 栏位已满购买弹提示() {
        DefaultPlayer player = new DefaultPlayer(1000);
        for (int i = 0; i < 6; i++) {
            player.addSpecialCard(filler("filler" + i, 1));
        }
        RandomShop shop = new RandomShop(player);
        FakeShopView view = new FakeShopView();
        DefaultShopController c = new DefaultShopController(shop, player, view);
        c.onBuy(shop.goods().get(0));
        assertEquals("功能牌栏位已满", view.lastError);
    }

    @Test
    void 代币不足刷新弹提示() {
        DefaultPlayer player = new DefaultPlayer(1); // 刷新费 2，不够
        RandomShop shop = new RandomShop(player);
        FakeShopView view = new FakeShopView();
        DefaultShopController c = new DefaultShopController(shop, player, view);
        c.onRefresh();
        assertEquals("代币不足，无法刷新", view.lastError);
    }

    @Test
    void 出售后界面刷新为已售状态() {
        DefaultPlayer player = new DefaultPlayer(1000);
        RandomShop shop = new RandomShop(player);
        FakeShopView view = new FakeShopView();
        DefaultShopController c = new DefaultShopController(shop, player, view);
        SpecialCard item = shop.goods().get(0);
        c.onBuy(item);
        c.onSell(item);
        assertTrue(view.lastOwned.isEmpty());
    }

    @Test
    void 离开商店触发回调() {
        DefaultPlayer player = new DefaultPlayer(100);
        RandomShop shop = new RandomShop(player);
        DefaultShopController c = new DefaultShopController(shop, player, new FakeShopView());
        AtomicBoolean left = new AtomicBoolean(false);
        c.setLeaveHandler(() -> left.set(true));
        c.onLeave();
        assertTrue(left.get());
    }
}
