package com.bilitro.view;

/**
 * 音效与背景音乐（需求 2.2.8）。
 * 开关状态即时生效并持久保存。
 * TODO: 音频资源格式与目录约定待确认；持久化位置（与存档同文件？）待确认。
 */
public interface AudioManager {

    void playSfx(String sfxId);

    void playBgm(String bgmId);

    void setSfxEnabled(boolean enabled);

    void setBgmEnabled(boolean enabled);

    boolean isSfxEnabled();

    boolean isBgmEnabled();
}
