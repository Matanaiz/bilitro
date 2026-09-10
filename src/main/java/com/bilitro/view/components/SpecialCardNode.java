package com.bilitro.view.components;

import com.bilitro.model.player.ConfiguredSpecialCard;
import com.bilitro.model.player.SpecialCard;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * 功能牌控件：与扑克牌一致的圆角卡牌样式（名称竖排居中）。
 * 点击缩放反馈由使用方绑定；后续换美术资源时只需改本类绘制部分。
 */
public class SpecialCardNode extends StackPane {

    /** 卡牌尺寸（比手牌略小）。 */
    private static final int WIDTH = 60;
    private static final int HEIGHT = 88;

    /** 创建一张功能牌控件。 */
    public SpecialCardNode(SpecialCard card) {
        Label label = new Label(displayName(card));
        label.setWrapText(true);
        label.setMaxWidth(WIDTH - 10);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7b3fb5;"
                + "-fx-text-alignment: center;");

        setPrefSize(WIDTH, HEIGHT);
        setMinSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        setAlignment(Pos.CENTER);
        getChildren().add(label);
        setStyle("-fx-background-color: #fdf6e3; -fx-background-radius: 8;"
                + "-fx-border-color: #7b3fb5; -fx-border-width: 2; -fx-border-radius: 8;");
    }

    /** 显示名：配置表有名用名，否则用 id。 */
    private static String displayName(SpecialCard card) {
        if (card instanceof ConfiguredSpecialCard c && c.name() != null && !c.name().isBlank()) {
            return c.name();
        }
        return card.id();
    }
}
