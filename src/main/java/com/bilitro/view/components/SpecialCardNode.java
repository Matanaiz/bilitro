package com.bilitro.view.components;

import com.bilitro.model.player.ConfiguredSpecialCard;
import com.bilitro.model.player.SpecialCard;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * 功能牌控件：与手牌一致的圆角卡牌样式（同尺寸、名称居中）。
 * 点击缩放反馈由使用方绑定；后续换美术资源时只需改本类绘制部分。
 */
public class SpecialCardNode extends StackPane {

    /** 卡牌尺寸（与手牌 CardNode 一致）。 */
    private static final int WIDTH = 70;
    private static final int HEIGHT = 100;

    /** 创建一张功能牌控件。 */
    public SpecialCardNode(SpecialCard card) {
        Label label = new Label(displayName(card));
        label.setWrapText(true);
        label.setMaxWidth(WIDTH - 10);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #7b3fb5;"
                + "-fx-text-alignment: center;");

        setPrefSize(WIDTH, HEIGHT);
        setMinSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        setAlignment(Pos.CENTER);
        getChildren().add(label);

        // 随机花色类功能牌（古老小丑、城堡）：牌面右上角直接显示当前花色标记
        if (card.currentSuit() != null) {
            Label badge = new Label(suitText(card.currentSuit()));
            badge.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: "
                    + suitColor(card.currentSuit()) + "; -fx-background-color: white;"
                    + "-fx-background-radius: 6; -fx-padding: 0 4 0 4;");
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            getChildren().add(badge);
        }

        setStyle("-fx-background-color: #fdf6e3; -fx-background-radius: 8;"
                + "-fx-border-color: #7b3fb5; -fx-border-width: 2; -fx-border-radius: 8;");
    }

    /** 花色显示字符。 */
    private static String suitText(com.bilitro.model.card.Suit suit) {
        return switch (suit) {
            case SPADE -> "♠";
            case HEART -> "♥";
            case CLUB -> "♣";
            case DIAMOND -> "♦";
        };
    }

    /** 花色显示颜色：红桃/方块用红色，黑桃/梅花用深色。 */
    private static String suitColor(com.bilitro.model.card.Suit suit) {
        return switch (suit) {
            case HEART, DIAMOND -> "#d32f2f";
            case SPADE, CLUB -> "#222";
        };
    }

    /** 显示名：配置表有名用名，否则用 id。 */
    private static String displayName(SpecialCard card) {
        if (card instanceof ConfiguredSpecialCard c && c.name() != null && !c.name().isBlank()) {
            return c.name();
        }
        return card.id();
    }
}
