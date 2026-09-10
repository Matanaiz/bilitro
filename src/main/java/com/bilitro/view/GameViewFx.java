package com.bilitro.view;

import com.bilitro.controller.GameController;
import com.bilitro.model.card.Card;
import com.bilitro.model.player.SpecialCard;
import com.bilitro.view.components.CardNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 对局界面的 JavaFX 实现（P0 纯色块文字版）。
 * 布局：顶部状态区，右侧功能牌栏，底部操作按钮，中央手牌区。
 */
public class GameViewFx implements GameView {

    private GameController controller;

    private final BorderPane root = new BorderPane();
    private final HBox handArea = new HBox(8);
    private final HBox specialArea = new HBox(8);

    private final Label levelLabel = new Label();
    private final Label targetLabel = new Label();
    private final Label totalLabel = new Label();
    private final Label playsLabel = new Label();
    private final Label discardsLabel = new Label();
    private final Label coinsLabel = new Label();
    private final Label scoreEffectLabel = new Label();

    private final Button playButton = new Button("出牌");
    private final Button discardButton = new Button("弃牌");
    private final Button deckButton = new Button("查看牌组");

    /** 创建对局界面（控制器随后通过 bindController 注入）。 */
    public GameViewFx() {
        buildLayout();
    }

    /** 注入控制器并绑定按钮事件（装配顺序：先视图后控制器）。 */
    public void bindController(GameController controller) {
        this.controller = controller;
        bindEvents();
    }

    /** 返回根节点，供 App 装入场景。 */
    public BorderPane root() {
        return root;
    }

    /** 组装界面布局。 */
    private void buildLayout() {
        HBox status = new HBox(20, levelLabel, targetLabel, totalLabel,
                playsLabel, discardsLabel, coinsLabel);
        status.setPadding(new Insets(10));

        VBox right = new VBox(10, new Label("功能牌"), specialArea);
        right.setPadding(new Insets(10));

        HBox actions = new HBox(12, playButton, discardButton, deckButton, scoreEffectLabel);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(10));

        handArea.setAlignment(Pos.BOTTOM_CENTER);
        handArea.setPadding(new Insets(30, 10, 30, 10));

        root.setTop(status);
        root.setRight(right);
        root.setBottom(actions);
        root.setCenter(handArea);
    }

    /** 绑定按钮与卡牌点击事件到控制器。 */
    private void bindEvents() {
        playButton.setOnAction(e -> controller.onPlay());
        discardButton.setOnAction(e -> controller.onDiscard());
        deckButton.setOnAction(e -> controller.onViewDeck());
    }

    /** 刷新手牌区：重建卡牌控件，保持选中状态，点击切换选中。 */
    @Override
    public void renderHand(List<Card> hand, List<Card> selected) {
        handArea.getChildren().clear();
        for (Card card : hand) {
            CardNode node = new CardNode(card);
            node.setSelected(selected.contains(card));
            node.setOnMouseClicked(e -> {
                node.toggle();
                controller.toggleSelect(card);
            });
            handArea.getChildren().add(node);
        }
    }

    /** 刷新状态区各数值。 */
    @Override
    public void renderStatus(int remainingTarget, int plays, int discards, int coins) {
        targetLabel.setText("剩余目标分: " + Math.max(0, remainingTarget));
        playsLabel.setText("出牌: " + plays);
        discardsLabel.setText("弃牌: " + discards);
        coinsLabel.setText("代币: " + coins);
    }

    /** 刷新关卡号与本局总分（状态区扩展信息）。 */
    public void renderProgress(int level, int totalScore) {
        levelLabel.setText("第 " + level + " 关");
        totalLabel.setText("总分: " + totalScore);
    }

    /** 刷新功能牌栏：点击缩放反馈由按钮自带，弹出效果说明。 */
    public void renderSpecialCards(List<SpecialCard> specials) {
        specialArea.getChildren().clear();
        for (SpecialCard s : specials) {
            Button b = new Button(s.id());
            b.setStyle("-fx-font-size: 11px;");
            b.setOnAction(e -> {
                b.setScaleX(0.9);
                b.setScaleY(0.9);
                controller.onInspectSpecialCard(s.id());
                b.setScaleX(1);
                b.setScaleY(1);
            });
            specialArea.getChildren().add(b);
        }
    }

    /** 按校验结果亮/灰出牌与弃牌按钮。 */
    @Override
    public void setActionEnabled(boolean canPlay, boolean canDiscard) {
        playButton.setDisable(!canPlay);
        discardButton.setDisable(!canDiscard);
    }

    /** 计分反馈：P0 显示本次得分文本，P2 换数字跳动动画。 */
    @Override
    public void playScoreEffect(int score) {
        scoreEffectLabel.setText("+" + score);
        scoreEffectLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e65100;");
    }

    /** 弹功能牌说明浮层。 */
    @Override
    public void showSpecialCardTip(String description) {
        alert("功能牌说明", description);
    }

    /** 弹剩余牌组浮层：按花色分组展示。 */
    @Override
    public void showDeck(List<Card> remaining) {
        Map<String, List<Card>> bySuit = new TreeMap<>(remaining.stream()
                .collect(Collectors.groupingBy(c -> c.suit().name())));
        StringBuilder sb = new StringBuilder("剩余 " + remaining.size() + " 张\n");
        bySuit.forEach((suit, cards) -> {
            sb.append('\n').append(suit).append("：");
            cards.stream()
                    .sorted(java.util.Comparator.comparingInt(c -> c.rank().value()))
                    .forEach(c -> sb.append(c.rank().name()).append(' '));
        });
        alert("查看牌组", sb.toString());
    }

    /** 弹通用文本提示。 */
    @Override
    public void showMessage(String message) {
        alert("提示", message);
    }

    /** 弹窗工具。 */
    private void alert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, content);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
