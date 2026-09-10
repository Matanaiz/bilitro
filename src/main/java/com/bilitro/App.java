package com.bilitro;

import com.bilitro.controller.DefaultGameController;
import com.bilitro.controller.DefaultShopController;
import com.bilitro.model.GameConfig;
import com.bilitro.model.card.StandardDeck;
import com.bilitro.model.game.DefaultGameSession;
import com.bilitro.model.game.GameSession;
import com.bilitro.model.game.LevelRule;
import com.bilitro.model.hand.DefaultHandTypeEvaluator;
import com.bilitro.model.hand.DefaultScoreCalculator;
import com.bilitro.model.player.DefaultPlayer;
import com.bilitro.model.player.Player;
import com.bilitro.model.shop.RandomShop;
import com.bilitro.model.shop.Shop;
import com.bilitro.view.GameViewFx;
import com.bilitro.view.ShopViewFx;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Set;

/**
 * Bilitro 应用入口。
 * 职责：启动 JavaFX，装配 MVC，管理 对局 → 商店 → 下一关 → 结算 的场景流转。
 * TODO(P1): 主菜单、存档检测。
 */
public class App extends Application {

    /** 第一关目标分（之后每关 ×1.5，见 GameConfig）。 */
    private static final int LEVEL1_TARGET = com.bilitro.model.GameConfig.LEVEL1_TARGET_SCORE;

    private Stage stage;
    private GameSession session;
    private Player player;
    private GameViewFx gameView;
    private DefaultGameController gameController;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Bilitro");
        startNewGame();
        stage.show();
    }

    /** 装配 MVC 并开始新一局。 */
    private void startNewGame() {
        player = new DefaultPlayer();
        session = new DefaultGameSession(
                new StandardDeck(),
                new DefaultHandTypeEvaluator(),
                new DefaultScoreCalculator(),
                player,
                new LevelRule(LEVEL1_TARGET, Set.of(), "第 1 关"));

        gameView = new GameViewFx(); // 先建视图，控制器在下面注入
        gameController = new DefaultGameController(session,
                new DefaultHandTypeEvaluator(), new DefaultScoreCalculator(), gameView);
        gameView.bindController(gameController);
        gameController.setOutcomeHandler(this::onRoundEnd);
        gameController.refresh();

        stage.setScene(new Scene(gameView.root(), 1000, 700));
    }

    /** 回合结局流转：过关进商店，胜利/失败弹结算。 */
    private void onRoundEnd(GameSession.RoundOutcome outcome) {
        switch (outcome) {
            case LEVEL_CLEARED -> openShop();
            case VICTORY -> showSettlement(true);
            case FAILED -> showSettlement(false);
            default -> { }
        }
    }

    /** 打开商店场景。 */
    private void openShop() {
        Shop shop = new RandomShop(player);
        ShopViewFx shopView = new ShopViewFx();
        DefaultShopController shopController = new DefaultShopController(shop, player, shopView);
        shopView.bindController(shopController);
        shopController.setLeaveHandler(this::leaveShop);
        shopController.refresh();
        stage.getScene().setRoot(shopView.root());
    }

    /** 离开商店：进入下一关并切回对局界面。 */
    private void leaveShop() {
        session.advanceLevel(nextRule(session.currentLevel() + 1));
        stage.getScene().setRoot(gameView.root());
        gameController.refresh();
    }

    /** 终局结算：弹提示，再来一局或退出。 */
    private void showSettlement(boolean victory) {
        Platform.runLater(() -> {
            ButtonType again = new ButtonType("再来一局");
            ButtonType quit = new ButtonType("退出", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    (victory ? "通关！本局胜利。" : "本局失败。")
                            + " 最后一关得分 " + session.levelScore(),
                    again, quit);
            alert.setTitle(victory ? "胜利" : "失败");
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(bt -> {
                if (bt == again) {
                    startNewGame();
                } else {
                    stage.close();
                }
            });
        });
    }

    /**
     * 生成下一关规则：目标分从 200 起每关 ×1.5（小组定稿），无禁用花色。
     * TODO: 禁用花色等差异化规则稍后查表（答复 9）。
     */
    private LevelRule nextRule(int level) {
        int target = (int) (GameConfig.LEVEL1_TARGET_SCORE
                * Math.pow(GameConfig.TARGET_SCORE_GROWTH, level - 1));
        return new LevelRule(target, Set.of(), "第 " + level + " 关");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
