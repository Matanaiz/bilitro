package com.bilitro.model.shop;

import com.bilitro.model.player.ConfiguredSpecialCard;
import com.bilitro.model.player.DefaultPlayer;
import com.bilitro.model.player.SpecialCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 商店测试：已持有功能牌不再出现、信用卡负债额度。 */
class RandomShopTest {

    /** 构造一张指定价格的测试功能牌。 */
    private static SpecialCard card(String id, int price) {
        return new ConfiguredSpecialCard(id, id, id, price, "",
                ConfiguredSpecialCard.EffectType.ADD_CHIPS, 0,
                ConfiguredSpecialCard.ConditionType.ALWAYS, null);
    }

    /** 构造一张信用卡（负债额度 20）。 */
    private static SpecialCard creditCard() {
        return new ConfiguredSpecialCard("credit_card", "信用卡", "", 1, "",
                ConfiguredSpecialCard.EffectType.ADD_CHIPS, 0,
                ConfiguredSpecialCard.ConditionType.ALWAYS, null,
                ConfiguredSpecialCard.GrowthType.NONE, 0,
                ConfiguredSpecialCard.SuitMode.NONE, 0, 0, 20, 0);
    }

    @Test
    void 已持有的功能牌不再出现在商品中() {
        DefaultPlayer player = new DefaultPlayer(1000);
        RandomShop shop = new RandomShop(player);
        // 买下一件商品后，多次刷新都不应再出现同 id 的牌
        SpecialCard bought = shop.goods().get(0);
        assertEquals(Shop.BuyResult.SUCCESS, shop.buy(bought));
        for (int i = 0; i < 20; i++) {
            assertTrue(shop.refresh());
            assertTrue(shop.goods().stream().noneMatch(g -> g.id().equals(bought.id())));
        }
    }

    @Test
    void 代币不足时不能购买() {
        DefaultPlayer player = new DefaultPlayer(0);
        RandomShop shop = new RandomShop(player);
        SpecialCard item = shop.goods().stream()
                .filter(g -> g.price() > 0).findFirst().orElseThrow();
        assertEquals(Shop.BuyResult.NOT_ENOUGH_COINS, shop.buy(item));
    }

    @Test
    void 信用卡允许负债购买() {
        DefaultPlayer player = new DefaultPlayer(0);
        player.addSpecialCard(creditCard());
        RandomShop shop = new RandomShop(player);
        SpecialCard item = shop.goods().stream()
                .filter(g -> g.price() > 0 && g.price() <= 20).findFirst().orElseThrow();
        assertEquals(Shop.BuyResult.SUCCESS, shop.buy(item));
        assertEquals(-item.price(), player.coins());
    }

    @Test
    void 出售功能牌按半价返还() {
        DefaultPlayer player = new DefaultPlayer(10);
        RandomShop shop = new RandomShop(player);
        SpecialCard item = shop.goods().get(0);
        shop.buy(item);
        int coinsAfterBuy = player.coins();
        shop.sell(item);
        assertEquals(coinsAfterBuy + item.price() / 2, player.coins());
        assertTrue(player.specialCards().isEmpty());
    }

    @Test
    void 代币不足时刷新失败且不扣费() {
        DefaultPlayer player = new DefaultPlayer(1); // 刷新费 2，不够
        RandomShop shop = new RandomShop(player);
        assertFalse(shop.refresh());
        assertEquals(1, player.coins());
    }

    @Test
    void 刷新成功后费用递增() {
        DefaultPlayer player = new DefaultPlayer(100);
        RandomShop shop = new RandomShop(player);
        int cost = shop.refreshCost();
        assertTrue(shop.refresh());
        assertEquals(cost + 3, shop.refreshCost());
    }

    @Test
    void 功能牌栏满时购买失败() {
        DefaultPlayer player = new DefaultPlayer(100);
        for (int i = 0; i < 6; i++) {
            player.addSpecialCard(card("filler" + i, 1));
        }
        RandomShop shop = new RandomShop(player);
        assertEquals(Shop.BuyResult.SLOTS_FULL, shop.buy(shop.goods().get(0)));
    }
}
