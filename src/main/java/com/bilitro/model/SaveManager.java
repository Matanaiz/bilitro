package com.bilitro.model;

import com.bilitro.model.game.GameSession;
import java.util.Optional;

/**
 * 自动存档（需求 2.2.7）与最高分记录（需求 2.2.4）。
 * 已定稿（答复 14/15）：存档格式 JSON，文件创建在本地目录；
 * 音效/BGM 开关状态与存档同文件持久化。
 * 实现提示：需在 pom.xml 引入 JSON 库（如 Jackson 或 Gson）。
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
