package com.bilitro.view;

/** 结算视图（失败 / 通关共用）。 */
public interface SettlementView {

    /**
     * @param victory    是否通关
     * @param totalScore 本局总分
     * @param highScore  历史最高分
     * @param newRecord  是否新纪录（展示"新纪录"标记）
     */
    void render(boolean victory, int totalScore, int highScore, boolean newRecord);

    /** P2：播放胜利 / 失败动画与败因提示。 */
    void playAnimation(String reason);
}
