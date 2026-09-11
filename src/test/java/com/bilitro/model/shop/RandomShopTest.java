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
}
