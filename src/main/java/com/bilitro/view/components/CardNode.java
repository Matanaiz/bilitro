package com.bilitro.view.components;

import com.bilitro.model.card.Card;
import com.bilitro.model.card.Rank;
import com.bilitro.model.card.Suit;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * 卡牌控件：纯色块 + 文字版扑克牌。
 * 点击切换选中：选中时上移并显示高亮边框，取消选中恢复原位。
 * P0 用颜色区分花色，后续换美术资源时只需改本类绘制部分。
 */
public class CardNode extends StackPane {

    /** 卡牌尺寸与选中上移位。 */
    private static final int WIDTH = 70;
    private static final int HEIGHT = 100;
    private static final int SELECT_LIFT = 18;

    private final Card card;
    private boolean selected;

    /** 创建一张牌的控件。 */
    public CardNode(Card card) {
        this.card = card;

        Label label = new Label(rankText(card.rank()) + "\n" + suitText(card.suit()));
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-alignment: center;"
                + "-fx-text-fill: " + suitColor(card.suit()) + ";");

        setPrefSize(WIDTH, HEIGHT);
        setAlignment(Pos.CENTER);
        getChildren().add(label);
        applyStyle();
    }

    /** 返回该控件代表的牌。 */
    public Card card() {
        return card;
    }

    /** 返回当前是否选中。 */
    public boolean isSelected() {
        return selected;
    }

    /** 设置选中状态并刷新外观（选中上移 + 金色高亮边框）。 */
    public void setSelected(boolean selected) {
        this.selected = selected;
        setTranslateY(selected ? -SELECT_LIFT : 0);
        applyStyle();
    }

    /** 翻转选中状态。 */
    public void toggle() {
        setSelected(!selected);
    }

    /** 按选中状态应用底色与边框。 */
    private void applyStyle() {
        String border = selected
                ? "-fx-border-color: gold; -fx-border-width: 3;"
                : "-fx-border-color: #555; -fx-border-width: 1;";
        setStyle("-fx-background-color: white; -fx-background-radius: 8;"
                + "-fx-border-radius: 8;" + border);
    }

    /** 点数显示文本（J/Q/K/A 用字母）。 */
    private static String rankText(Rank rank) {
        return switch (rank) {
            case JACK -> "J";
            case QUEEN -> "Q";
            case KING -> "K";
            case ACE -> "A";
            default -> String.valueOf(rank.value());
        };
    }

    /** 花色显示字符。 */
    private static String suitText(Suit suit) {
        return switch (suit) {
            case SPADE -> "♠";
            case HEART -> "♥";
            case CLUB -> "♣";
            case DIAMOND -> "♦";
        };
    }

    /** 花色显示颜色：红桃/方块用红色，黑桃/梅花用深色。 */
    private static String suitColor(Suit suit) {
        return switch (suit) {
            case HEART, DIAMOND -> "#d32f2f";
            case SPADE, CLUB -> "#222";
        };
    }
}
