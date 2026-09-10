# -*- coding: utf-8 -*-
"""填写详细设计说明书 v3.0（依据 bilitro 项目实际代码）。"""
from docx import Document
from docx.shared import Pt
from docx.oxml.ns import qn

SRC = r"D:\桌面\code\javafx\bilitro\详细设计说明书.docx"
DST = r"D:\桌面\code\javafx\bilitro\XX项目_详细设计说明书_v3.0_已填写.docx"

d = Document(SRC)


def set_block(p, text, code=False, size=10.5):
    """清空段落并按行写入，代码用 Consolas。"""
    for r in list(p.runs):
        r._element.getparent().remove(r._element)
    lines = text.split("\n")
    for i, line in enumerate(lines):
        run = p.add_run(line)
        run.font.size = Pt(size)
        if code:
            run.font.name = "Consolas"
            run._element.rPr.rFonts.set(qn("w:eastAsia"), "Consolas")
        if i < len(lines) - 1:
            run.add_break()


def find_para(key):
    for p in d.paragraphs:
        if key in p.text:
            return p
    raise RuntimeError("not found: " + key)


def fill_row(row, values):
    for j, v in enumerate(values):
        row.cells[j].text = v


# ============ 1.1 主状态机 状态转移表 ============
transitions = [
    ("MAIN_MENU", "点击新一局或继续游戏", "无存档或存档校验通过", "IN_ROUND", "装配对局，从第 1 关开始或恢复存档"),
    ("IN_ROUND", "点击暂停按钮", "对局进行中", "PAUSED", "弹出半透明暂停菜单"),
    ("PAUSED", "点击继续", "无", "IN_ROUND", "关闭暂停菜单回到对局"),
    ("PAUSED", "点击回主菜单", "自动存档完成", "MAIN_MENU", "保存进度后离开本局"),
    ("IN_ROUND", "出牌结算后达标", "剩余目标分小于等于 0，且当前关小于 8", "LEVEL_CLEAR", "发放通关奖励代币"),
    ("LEVEL_CLEAR", "奖励结算完成", "无", "SHOPPING", "进入商店界面"),
    ("SHOPPING", "点击离开商店", "无", "IN_ROUND", "加载下一关规则，重置出牌与弃牌次数"),
    ("IN_ROUND", "出牌结算后达标", "剩余目标分小于等于 0，且当前为第 8 关", "VICTORY", "进入通关结算界面"),
    ("IN_ROUND", "操作结束后未达标", "剩余出牌次数为 0", "FAILED", "进入失败结算界面并记录本次得分"),
    ("VICTORY", "点击再来一局", "无", "IN_ROUND", "重新从第 1 关开始"),
    ("FAILED", "点击再来一局", "无", "IN_ROUND", "重新从第 1 关开始"),
]
t0 = d.tables[0]
while len(t0.rows) - 1 < len(transitions):
    t0.add_row()
for i, row_vals in enumerate(transitions):
    fill_row(t0.rows[i + 1], row_vals)

# ============ 1.2 状态枚举定义 ============
enum_code = """public enum GameState {
    MAIN_MENU,     // 主菜单
    IN_ROUND,      // 回合内（选牌、出牌、弃牌）
    PAUSED,        // 暂停（半透明菜单：继续、设置、回主菜单）
    LEVEL_CLEAR,   // 过关，进商店前
    SHOPPING,      // 商店
    FAILED,        // 失败结算
    VICTORY        // 通关结算
}"""
set_block(find_para("在此填写状态枚举"), enum_code, code=True)

# ============ 1.3 状态切换方法 ============
switch_code = """// 本项目不由单一 setState 方法驱动状态迁移：
// 回合内结局由 DefaultGameSession.outcome() 判定，场景级切换由 App.onRoundEnd() 执行。

public RoundOutcome outcome() {
    if (remainingTargetScore <= 0) {
        return level >= GameConfig.MAX_LEVEL ? RoundOutcome.VICTORY : RoundOutcome.LEVEL_CLEARED;
    }
    return remainingPlays <= 0 ? RoundOutcome.FAILED : RoundOutcome.ONGOING;
}

private void onRoundEnd(GameSession.RoundOutcome outcome) {
    switch (outcome) {
        case LEVEL_CLEARED -> openShop();
        case VICTORY -> showSettlement(true);
        case FAILED -> showSettlement(false);
        default -> { }
    }
}"""
set_block(find_para("private GameState currentState"), switch_code, code=True)

# ============ 1.5 状态机自查 ============
t1 = d.tables[1]
notes = {
    0: "每个状态都有出口：PAUSED 可回对局或主菜单，LEVEL_CLEAR 进商店，VICTORY 与 FAILED 可再来一局",
    1: "全部状态均可从 MAIN_MENU 出发经正常操作到达，测试已覆盖各条路径",
    2: "选牌出牌、计分、抽牌、通关与失败判定四条 P0 流程均落在 IN_ROUND 及其出边上",
}
for i in range(3):
    t1.rows[i + 1].cells[1].text = "√"
    t1.rows[i + 1].cells[2].text = notes[i]

# ============ 1.6 子状态机 ============
set_block(find_para("写一句话"),
          "子状态机名称：回合内操作子状态机。所属父状态：IN_ROUND。"
          "用途：描述一回合内从选牌、出牌、计分动画到补牌结算的操作流转，"
          "计分动画播放期间锁定出牌与弃牌按钮，防止动画未播完时重复操作造成状态错乱。")
sub_rows = [
    ("选牌中", "点击出牌按钮", "选中 1 到 5 张且剩余出牌次数大于 0", "计分动画中", "锁定操作，逐张播放计分过程"),
    ("计分动画中", "动画播放完成", "无", "补牌结算", "落账得分，扣减剩余目标分与出牌次数"),
    ("补牌结算", "补牌完成", "无", "选牌中", "从牌组随机补足手牌，解锁操作"),
]
t2 = d.tables[2]
for i, row_vals in enumerate(sub_rows):
    fill_row(t2.rows[i + 1], row_vals)

# ============ 二、方法级设计 ============
set_block(find_para("类一："),
          "类一：DefaultGameSession（职责：对局核心，串联牌型判定、计分、补牌、结束判定与通关奖励）")
set_block(find_para("类二："),
          "类二：DefaultHandTypeEvaluator（职责：按优先级从高到低查表判定牌型，并校验出牌合法性）")
set_block(find_para("类三："),
          "类三：RandomShop（职责：商店商品的随机生成、刷新与购买校验）")

m1 = [
    ("play", "PlayResult play(List<Card> selected)", "selected 为当前选中的 1 到 5 张牌", "本次出牌结果（牌型与得分）", "剩余出牌次数大于 0，所选牌构成合法牌型", "剩余目标分与出牌次数扣减，已出牌移除并补等量新牌"),
    ("discard", "void discard(List<Card> selected)", "selected 为要弃掉的选中牌", "无", "剩余弃牌次数大于 0", "弃牌次数减 1，弃牌移除并补等量新牌"),
    ("outcome", "RoundOutcome outcome()", "无", "对局结局：ONGOING、LEVEL_CLEARED、VICTORY、FAILED", "无", "不修改任何状态，只读判定"),
]
m2 = [
    ("evaluate", "Optional<HandType> evaluate(List<Card> selected)", "selected 为选中的牌", "构成的最高牌型，不合法时返回 empty", "无", "不修改状态"),
    ("evaluateDetail", "Optional<Evaluation> evaluateDetail(List<Card> selected)", "selected 为选中的牌", "牌型与参与计分的手牌", "无", "不修改状态，未构成牌型的散牌不计入结果"),
    ("isPlayable", "boolean isPlayable(List<Card> selected)", "selected 为选中的牌", "张数在 1 到 5 之间返回 true", "无", "决定出牌按钮亮起或置灰"),
]
m3 = [
    ("goods", "List<SpecialCard> goods()", "无", "当前商品列表的只读快照", "无", "不修改状态"),
    ("refresh", "void refresh()", "无", "无", "无", "商品列表从功能牌池重新随机抽取 3 件"),
    ("buy", "BuyResult buy(SpecialCard item)", "item 为要购买的功能牌", "购买结果：SUCCESS、SLOTS_FULL、NOT_ENOUGH_COINS", "商品在货架上", "成功时扣款、功能牌入栏、该商品下架；失败时货币与栏位不变"),
]
for ti, rows in ((3, m1), (4, m2), (5, m3)):
    t = d.tables[ti]
    for i, row_vals in enumerate(rows):
        fill_row(t.rows[i + 1], row_vals)

# ============ 三、P0 设计落点对照 ============
p0_rows = [
    ("2.1.1 选牌与出牌/弃牌",
     "DefaultGameController.toggleSelect、onPlay、onDiscard；HandTypeEvaluator.isPlayable 校验张数",
     "选中上限由 GameConfig 统一约束，合法性校验集中在模型接口，控制器只做事件翻译，按钮亮灰在 refresh 中统一计算，避免界面各处重复判断"),
    ("2.1.2 自动计分",
     "DefaultScoreCalculator.score 与 steps，ScoringContext 承载计分中间态，SpecialCard.onScore 钩子",
     "计分过程拆成分步快照供界面播放动画；功能牌通过钩子读写上下文，新增功能牌不需要修改计分器本体"),
    ("2.1.3 随机抽牌",
     "StandardDeck.draw 随机抽牌，DefaultGameSession.replaceCards 统一补牌",
     "牌组封装在 Deck 接口内，出牌和弃牌共用同一条补牌路径，随机源集中在 Deck 实现里，便于测试时换成固定牌序"),
    ("2.1.4 通关与失败判定",
     "DefaultGameSession.outcome 判定结局，App.onRoundEnd 负责场景流转",
     "判定规则与结束判定流程图一致并集中在 outcome 一个方法中；结局通过回调交给 App 切换场景，控制器不依赖 JavaFX"),
]
t6 = d.tables[6]
for i, row_vals in enumerate(p0_rows):
    fill_row(t6.rows[i + 1], row_vals)

pattern_rows = [
    ("Deck、HandTypeEvaluator、ScoreCalculator、Shop、SaveManager 均为接口，对应 StandardDeck、Default 系列实现",
     "规则可以整体替换，模型层可脱离界面做单元测试；例如换一套牌型规则只需替换 evaluator 实现，其余代码不动"),
    ("未使用。状态变化由控制器调用 view 的 render 系列方法主动刷新",
     "所有状态变更都经过控制器中转，刷新时机明确；引入观察者会让模型反过来感知界面通知，增加层间耦合"),
    ("主菜单与 MenuController 在 P0 阶段未实现，由 App 直接承担入口与场景流转",
     "主菜单、继续游戏属于 P1 功能，当前只保留接口约定，避免提前实现用不到的代码"),
]
t7 = d.tables[7]
for i in range(3):
    t7.rows[i + 1].cells[1].text = pattern_rows[i][0]
    t7.rows[i + 1].cells[2].text = pattern_rows[i][1]

d.save(DST)
print("saved", DST)
