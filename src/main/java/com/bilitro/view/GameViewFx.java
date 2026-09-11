package com.bilitro.view;

import com.bilitro.controller.GameController;
import com.bilitro.model.card.Card;
import com.bilitro.model.game.GameSession;
import com.bilitro.model.player.SpecialCard;
import com.bilitro.view.components.CardNode;
import com.bilitro.view.components.SpecialCardNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 对局界面的 JavaFX 实现（P0 纯色块文字版）。
 * 布局参考小丑牌：左侧深蓝信息栏（目标分/总分/次数/关卡/代币），
 * 中央下方手牌区与操作按钮，上方功能牌栏，右侧牌组计数。
 */
public class GameViewFx implements GameView {

    /** 界面主色调。 */
    private static final String BG = "-fx-background-color: #1f6f54;";       // 牌桌绿
    private static final String PANEL = "-fx-background-color: #16324f; -fx-background-radius: 10;"; // 信息栏深蓝
    private static final String PANEL_INNER = "-fx-background-color: #0d2033; -fx-background-radius: 8;";
    private static final String TEXT_BIG = "-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;";
    private static final String TEXT_MID = "-fx-font-size: 15px; -fx-text-fill: #bcd;";

    private GameController controller;

    private final BorderPane root = new BorderPane();
    private final HBox handArea = new HBox(6);
    private final HBox specialArea = new HBox(8);

    private final Label levelLabel = valueLabel();
    private final Label targetLabel = valueLabel();
    private final Label totalLabel = valueLabel();
    private final Label playsLabel = valueLabel();
    private final Label discardsLabel = valueLabel();
    private final Label coinsLabel = valueLabel();
    private final Label deckCountLabel = valueLabel();

    private final Button playButton = new Button("出牌");
    private final Button discardButton = new Button("弃牌");
    private final Button deckButton = new Button("查看牌组");

    /** 中央计分过程区：选中牌型名 + 逐张计分动画的手牌横排。 */
    private final Label previewTypeLabel = new Label("未选牌");
    private final Label previewChipsLabel = valueLabel();
    private final Label previewMultLabel = valueLabel();
    private final HBox processArea = new HBox(6);

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
        root.setStyle(BG);
        root.setLeft(buildSidebar());
        root.setCenter(buildCenter());
        root.setRight(buildDeckCounter());
    }

    /**
     * 左侧信息栏：目标分、总分、当前积分×倍数（同一面板内横向排列，中间乘号）、
     * 出牌/弃牌次数、关卡、代币；整体竖向排列。
     */
    private VBox buildSidebar() {
        VBox box = new VBox(10,
                panel("目标得分", targetLabel),
                panel("本关总分", totalLabel),
                chipsMultPanel(),
                panel("出牌次数", playsLabel),
                panel("弃牌次数", discardsLabel),
                panel("关卡", levelLabel),
                panel("代币", coinsLabel));
        box.setPadding(new Insets(14));
        box.setPrefWidth(190);
        box.setMinWidth(190); // 左侧栏保底宽度
        box.setStyle("-fx-background-color: #10243a;");
        return box;
    }

    /** 当前积分 × 当前倍数面板：两个大数字横向排列，中间放乘号。 */
    private VBox chipsMultPanel() {
        Label t = new Label("当前积分 × 倍数");
        t.setStyle(TEXT_MID);
        Label x = new Label("×");
        x.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e88;");
        HBox row = new HBox(10, previewChipsLabel, x, previewMultLabel);
        row.setAlignment(Pos.CENTER);
        VBox inner = new VBox(4, t, row);
        inner.setAlignment(Pos.CENTER_LEFT);
        inner.setPadding(new Insets(10, 14, 10, 14));
        inner.setStyle(PANEL_INNER);
        VBox wrapper = new VBox(inner);
        wrapper.setPadding(new Insets(4));
        wrapper.setStyle(PANEL);
        return wrapper;
    }

    /** 中央区域：上方功能牌栏，中间计分过程区（吃掉多余空间），下方手牌区与操作按钮。 */
    private VBox buildCenter() {
        Label specialTitle = new Label("功能牌");
        specialTitle.setStyle(TEXT_MID);
        VBox specialBox = new VBox(6, specialTitle, specialArea);
        specialBox.setAlignment(Pos.TOP_CENTER);
        specialBox.setPadding(new Insets(12));

        VBox processBox = buildProcessBox();

        handArea.setAlignment(Pos.BOTTOM_CENTER);
        handArea.setPadding(new Insets(10));
        handArea.setFillHeight(false); // 手牌保持原始比例，不被拉伸

        styleButton(playButton, "#2e7ddb");
        styleButton(discardButton, "#d3355b");
        styleButton(deckButton, "#555f6e");
        HBox actions = new HBox(14, playButton, discardButton, deckButton);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(12));
        actions.setMinHeight(70); // 底部操作区保底高度

        VBox center = new VBox(specialBox, processBox, handArea, actions);
        VBox.setVgrow(processBox, Priority.ALWAYS); // 中间计分过程区吃掉多余空间
        return center;
    }

    /**
     * 中间计分过程区：平时显示选中牌型名；出牌时从左到右逐张摆牌，
     * 左侧当前积分与当前倍数随计算一步步增长。
     */
    private VBox buildProcessBox() {
        previewTypeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        processArea.setAlignment(Pos.CENTER);
        processArea.setMinHeight(140); // 计分过程区保底高度
        VBox box = new VBox(10, previewTypeLabel, processArea);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    /** 右侧牌组计数（剩余牌数）。 */
    private VBox buildDeckCounter() {
        Label title = new Label("牌组");
        title.setStyle(TEXT_MID);
        VBox box = new VBox(6, title, deckCountLabel);
        box.setAlignment(Pos.BOTTOM_CENTER);
        box.setPadding(new Insets(14));
        box.setPrefWidth(90);
        return box;
    }

    /** 信息栏小面板：标题 + 大数字。 */
    private VBox panel(String title, Label value) {
        Label t = new Label(title);
        t.setStyle(TEXT_MID);
        VBox inner = new VBox(4, t, value);
        inner.setAlignment(Pos.CENTER_LEFT);
        inner.setPadding(new Insets(10, 14, 10, 14));
        inner.setStyle(PANEL_INNER);
        VBox wrapper = new VBox(inner);
        wrapper.setPadding(new Insets(4));
        wrapper.setStyle(PANEL);
        return wrapper;
    }

    /** 大数字标签。 */
    private static Label valueLabel() {
        Label l = new Label("0");
        l.setStyle(TEXT_BIG);
        return l;
    }

    /** 按钮统一配色。 */
    private void styleButton(Button b, String color) {
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;"
                + "-fx-font-size: 15px; -fx-font-weight: bold;"
                + "-fx-background-radius: 8; -fx-padding: 8 22 8 22;");
    }

    /** 绑定按钮与卡牌点击事件到控制器。 */
    private void bindEvents() {
        playButton.setOnAction(e -> controller.onPlay());
        discardButton.setOnAction(e -> controller.onDiscard());
        deckButton.setOnAction(e -> controller.onViewDeck());
    }

    /** 刷新手牌区：按点数从大到小排列展示，保持选中状态，点击切换选中。 */
    @Override
    public void renderHand(List<Card> hand, List<Card> selected) {
        handArea.getChildren().clear();
        List<Card> sorted = hand.stream()
                .sorted(java.util.Comparator.comparingInt((Card c) -> c.rank().value()).reversed())
                .toList();
        for (Card card : sorted) {
            CardNode node = new CardNode(card);
            node.setSelected(selected.contains(card));
            node.setOnMouseClicked(e -> {
                node.toggle();
                controller.toggleSelect(card);
            });
            handArea.getChildren().add(node);
        }
    }

    /**
     * 刷新计分预览：中间区域显示选中牌型名，左侧显示当前积分与当前倍数。
     * 不显示预估总分；选中为空或不合法时传 null 清空显示。
     */
    public void renderPreview(String handTypeName, Integer chips, Integer mult) {
        if (handTypeName == null) {
            previewTypeLabel.setText("未选牌");
            previewChipsLabel.setText("0");
            previewMultLabel.setText("0");
            return;
        }
        previewTypeLabel.setText(handTypeName);
        previewChipsLabel.setText(String.valueOf(chips));
        previewMultLabel.setText(String.valueOf(mult));
    }

    /** 清空中间计分过程区（通关/刷新时调用）。 */
    public void clearProcess() {
        processArea.getChildren().clear();
    }

    /**
     * 播放计分过程：在中间区域从左到右逐张摆出计分手牌，
     * 左侧当前积分与当前倍数随每一步增长；全部播完后执行 onFinished。
     */
    public void playScoringProcess(java.util.List<com.bilitro.model.hand.ScoreCalculator.ScoreStep> steps,
                                   Runnable onFinished) {
        processArea.getChildren().clear();
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        for (int i = 0; i < steps.size(); i++) {
            var step = steps.get(i);
            timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
                    javafx.util.Duration.millis(400.0 * (i + 1)), e -> {
                if (step.card() != null) {
                    processArea.getChildren().add(new CardNode(step.card()));
                }
                previewChipsLabel.setText(String.valueOf(step.chipsAfter()));
                previewMultLabel.setText(String.valueOf(step.multAfter()));
            }));
        }
        // 末尾补一个空关键帧：最后一张牌（及整手结算类功能牌）生效后多停一拍再落账
        timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(400.0 * (steps.size() + 1)), e -> { }));
        timeline.setOnFinished(e -> onFinished.run());
        timeline.play();
    }

    /** 刷新状态区各数值（目标得分为本关固定值，一关内不变）。 */
    @Override
    public void renderStatus(int targetScore, int plays, int discards, int coins) {
        targetLabel.setText(String.valueOf(targetScore));
        playsLabel.setText(String.valueOf(plays));
        discardsLabel.setText(String.valueOf(discards));
        coinsLabel.setText(String.valueOf(coins));
    }

    /** 刷新关卡号与本局总分。 */
    public void renderProgress(int level, int totalScore) {
        levelLabel.setText(String.valueOf(level));
        totalLabel.setText(String.valueOf(totalScore));
    }

    /** 刷新牌组剩余计数。 */
    public void renderDeckCount(int remaining) {
        deckCountLabel.setText(String.valueOf(remaining));
    }

    /** 刷新功能牌栏：扑克牌样式控件，点击缩放反馈并弹出效果说明。 */
    public void renderSpecialCards(List<SpecialCard> specials) {
        specialArea.getChildren().clear();
        for (SpecialCard s : specials) {
            SpecialCardNode node = new SpecialCardNode(s);
            node.setOnMouseClicked(e -> {
                node.setScaleX(0.88);
                node.setScaleY(0.88);
                controller.onInspectSpecialCard(s.id());
                node.setScaleX(1);
                node.setScaleY(1);
            });
            specialArea.getChildren().add(node);
        }
    }

    /** 按校验结果亮/灰出牌与弃牌按钮。 */
    @Override
    public void setActionEnabled(boolean canPlay, boolean canDiscard) {
        playButton.setDisable(!canPlay);
        discardButton.setDisable(!canDiscard);
    }

    /** 计分反馈：在中间计分过程区末尾显示本次得分文本（P2 换数字跳动动画）。 */
    @Override
    public void playScoreEffect(int score) {
        Label scoreLabel = new Label("+" + score);
        scoreLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ffb300;");
        processArea.getChildren().add(scoreLabel);
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

    /** 弹通关结算弹窗：本关得分 + 代币奖励明细（过关奖励 / 剩余出牌奖励 / 利息奖励）。 */
    @Override
    public void showLevelClearReward(int levelScore, GameSession.RewardBreakdown reward) {
        String content = "本关得分：" + levelScore + "\n\n"
                + "代币获得：\n"
                + "过关奖励 +" + reward.base() + "\n"
                + "剩余出牌次数奖励 +" + reward.playBonus() + "\n"
                + "利息奖励 +" + reward.interest() + "\n"
                + "合计 +" + reward.total() + " 代币";
        alert("过关！", content);
    }

    /** 弹窗工具（延后到下一个界面脉冲再弹，避免动画/布局回调中 showAndWait 报错）。 */
    private void alert(String title, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, content);
            a.setTitle(title);
            a.setHeaderText(null);
            a.showAndWait();
        });
    }
}
