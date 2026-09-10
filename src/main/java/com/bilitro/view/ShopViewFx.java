package com.bilitro.view;

import com.bilitro.controller.ShopController;
import com.bilitro.model.player.SpecialCard;
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
 * 顶部代币与操作，中央商品列表（名称/描述/价格/购买按钮）。
 */
public class ShopViewFx implements ShopView {

    private ShopController controller;

    private final BorderPane root = new BorderPane();
    private final Label coinsLabel = new Label();
    private final VBox goodsArea = new VBox(10);
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

        goodsArea.setAlignment(Pos.TOP_CENTER);
        goodsArea.setPadding(new Insets(20));

        root.setTop(top);
        root.setCenter(goodsArea);
    }

    /** 刷新商品列表与代币显示。 */
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
            goodsArea.getChildren().add(goodsRow(item));
        }
    }

    /** 单个商品行：名称、描述、价格、购买按钮。 */
    private HBox goodsRow(SpecialCard item) {
        Label name = new Label(itemName(item));
        name.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label desc = new Label(item.description());
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #bcd;");
        VBox texts = new VBox(4, name, desc);

        Label price = new Label("$" + item.price());
        price.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffd54f;");
        Button buy = new Button("购买");
        buy.setOnAction(e -> controller.onBuy(item));

        HBox row = new HBox(16, texts, price, buy);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setStyle("-fx-background-color: #16324f; -fx-background-radius: 10;");
        row.setPrefWidth(560);
        return row;
    }

    /** 商品显示名：配置表有名用名，否则用 id。 */
    private String itemName(SpecialCard item) {
        if (item instanceof com.bilitro.model.player.ConfiguredSpecialCard c && !c.name().isBlank()) {
            return c.name();
        }
        return item.id();
    }

    /** 购买失败提示："栏位已满" / "代币不足"。 */
    @Override
    public void showBuyFailure(String message) {
        javafx.application.Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.WARNING, message);
            a.setTitle("购买失败");
            a.setHeaderText(null);
            a.showAndWait();
        });
    }
}
