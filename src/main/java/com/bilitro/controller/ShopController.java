package com.bilitro.controller;

import com.bilitro.model.player.SpecialCard;

/** 商店控制器。 */
public interface ShopController {

    void onBuy(SpecialCard item);

    void onRefresh();

    /** 离开商店，进入下一关。 */
    void onLeave();
}
