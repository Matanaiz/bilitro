package com.bilitro;

import com.bilitro.controller.DefaultGameController;
import com.bilitro.model.card.StandardDeck;
import com.bilitro.model.game.DefaultGameSession;
import com.bilitro.model.game.GameSession;
import com.bilitro.model.game.LevelRule;
import com.bilitro.model.hand.DefaultHandTypeEvaluator;
import com.bilitro.model.hand.DefaultScoreCalculator;
import com.bilitro.model.player.DefaultPlayer;
import com.bilitro.view.GameViewFx;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Set;

/**
 * Bilitro 应用入口。
 * 职责：启动 JavaFX，装配 MVC（P0：直接进入对局）。
 * TODO(P1): 主菜单、存档检测、商店场景切换。
 */
public class App extends Application {

    /** 第一关目标分（P0 简化值，关卡规则表定稿后改为查表）。 */
    private static final int LEVEL1_TARGET = 150;

    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("Bilitro");
        startNewGame();
        stage.show();
    }

    /** 装配 MVC 并开始新一局。 */
    private void startNewGame() {
        GameSession session = new DefaultGameSession(
                new StandardDeck(),
                new DefaultHandTypeEvaluator(),
                new DefaultScoreCalculator(),
                new DefaultPlayer(),
                new LevelRule(LEVEL1_TARGET, Set.of(), "第 1 关"));

        GameViewFx view = new GameViewFx(); // 先建视图，控制器在下面注入
        DefaultGameController controller = new DefaultGameController(session,
                new DefaultHandTypeEvaluator(), new DefaultScoreCalculator(), view);
        view.bindController(controller);
        controller.setOutcomeHandler(this::onGameEnd);
        controller.refresh();

        stage.setScene(new Scene(view.root(), 1000, 700));
    }

    /** 终局处理：弹结算提示，再来一局或退出。 */
    private void onGameEnd(GameSession.RoundOutcome outcome) {
        boolean victory = outcome == GameSession.RoundOutcome.VICTORY;
        ButtonType again = new ButtonType("再来一局");
        ButtonType quit = new ButtonType("退出", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                victory ? "通关！本局胜利。" : "本局失败。", again, quit);
        alert.setTitle(victory ? "胜利" : "失败");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(bt -> {
            if (bt == again) {
                startNewGame();
            } else {
                stage.close();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
