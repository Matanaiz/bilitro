package com.bilitro.view;

import com.bilitro.controller.ShopController;
import com.bilitro.model.player.SpecialCard;
import com.bilitro.view.components.SpecialCardNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * 商店界面的 JavaFX 实现（P0 纯色块文字版）。
 * 顶部代币与操作（刷新显示费用），中央商品列表（卡牌样式 + 描述/价格/购买），
 * 下方"我的功能牌"栏（卡牌样式 + 出售按钮，半价返还）。
 */
public class ShopViewFx implements ShopView {

    private ShopController controller;

    private final BorderPane root = new BorderPane();
    private final Label coinsLabel = new Label();
    private final HBox goodsArea = new HBox(16);
    private final HBox ownedArea = new HBox(12);
    private final Button refreshButton = new Button("刷新商品");
    private final Button leaveButton = new Button("离开商店，进入下一关");

    /** 创建商店界面（控制器随后通过 bindController 注入）。 */
    public ShopViewFx() {
        buildLayout();
    }

    /** 注入控制器并绑定按钮事件。 */
    public void bindController(ShopController controller) {
        this.controller = controller;
        refreshButton.setOnAction(e -> controller.onRefresh());
        leaveButton.setOnAction(e -> controller.onLeave());
    }

    /** 返回根节点，供 App 装入场景。 */
    public BorderPane root() {
        return root;
    }

    /** 组装界面布局。 */
    private void buildLayout() {
        root.setStyle("-fx-background-color: #1f6f54;");

        Label title = new Label("商店");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        coinsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffd54f;");
        HBox top = new HBox(20, title, coinsLabel, refreshButton, leaveButton);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(14));

        goodsArea.setAlignment(Pos.CENTER);
        goodsArea.setPadding(new Insets(20));
        goodsArea.setFillHeight(false); // 商品列保持固有高度，不被拉伸

        Label ownedTitle = new Label("我的功能牌");
        ownedTitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #bcd;");
        ownedArea.setAlignment(Pos.CENTER);
        ownedArea.setFillHeight(false); // 持有牌保持固有尺寸
        VBox ownedBox = new VBox(8, ownedTitle, ownedArea);
        ownedBox.setAlignment(Pos.CENTER);
        ownedBox.setPadding(new Insets(10));

        root.setTop(top);
        root.setCenter(goodsArea);
        root.setBottom(ownedBox);
    }

    /** 刷新商品列表与代币显示：商品以功能牌卡牌样式展示，下方附描述/价格/购买按钮。 */
    @Override
    public void renderGoods(List<SpecialCard> goods, int coins) {
        coinsLabel.setText("代币: " + coins);
        goodsArea.getChildren().clear();
        if (goods.isEmpty()) {
            Label empty = new Label("本批商品已售空");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #bcd;");
            goodsArea.getChildren().add(empty);
            return;
        }
        for (SpecialCard item : goods) {
            goodsArea.getChildren().add(goodsColumn(item));
        }
    }

    /** 单个商品：功能牌控件 + 描述 + 价格与购买按钮。 */
    private VBox goodsColumn(SpecialCard item) {
        SpecialCardNode node = new SpecialCardNode(item);
        Label desc = new Label(item.description());
        desc.setWrapText(true);
        desc.setMaxWidth(150);
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #bcd;");
        Button buy = new Button("$" + item.price() + " 购买");
        buy.setOnAction(e -> controller.onBuy(item));

        VBox col = new VBox(8, node, desc, buy);
        col.setAlignment(Pos.CENTER);
        col.setPadding(new Insets(12));
        col.setMaxWidth(190); // 商品列固定宽度，不随窗口拉伸
        col.setStyle("-fx-background-color: #16324f; -fx-background-radius: 10;");
        return col;
    }

    /** 刷新"我的功能牌"栏：每张牌下方带半价出售按钮。 */
    @Override
    public void renderOwned(List<SpecialCard> owned) {
        ownedArea.getChildren().clear();
        for (SpecialCard item : owned) {
            Button sell = new Button("出售 +$" + sellPrice(item));
            sell.setStyle("-fx-font-size: 11px;");
            sell.setOnAction(e -> controller.onSell(item));
            VBox col = new VBox(6, new SpecialCardNode(item), sell);
            col.setAlignment(Pos.CENTER);
            ownedArea.getChildren().add(col);
        }
    }

    /** 出售价 = 购买价的一半（与 Shop.sellPrice 口径一致）。 */
    private int sellPrice(SpecialCard item) {
        return item.price() / 2;
    }

    /** 刷新刷新按钮上的费用显示。 */
    @Override
    public void renderRefreshCost(int cost) {
        refreshButton.setText("刷新商品（$" + cost + "）");
    }

    /** 购买失败提示："栏位已满" / "代币不足"。 */
    @Override
    public void showBuyFailure(String message) {
        javafx.application.Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.WARNING, message);
            a.setTitle("提示");
            a.setHeaderText(null);
            a.showAndWait();
        });
    }
}
