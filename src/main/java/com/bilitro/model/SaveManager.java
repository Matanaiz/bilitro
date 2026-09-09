package com.bilitro.model;

import com.bilitro.model.game.GameSession;
import java.util.Optional;

/**
 * 自动存档（需求 2.2.7）与最高分记录（需求 2.2.4）。
 * TODO: 存档格式（JSON / Java 序列化）与存储路径待确认。
 */
public interface SaveManager {

    /** 保存当前对局进度（关卡、出牌/弃牌次数、手牌、货币、功能牌）。 */
    void save(GameSession session, int level);

    /** 读取存档；无存档时返回 empty。 */
    Optional<GameSession> load();

    void clearSave();

    int loadHighScore();

    void saveHighScore(int score);
}
