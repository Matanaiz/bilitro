# -*- coding: utf-8 -*-
"""把概要设计说明书第一、二部分写入模板（审核后的定稿内容）。"""
from docx import Document
from docx.shared import Pt
from docx.oxml.ns import qn
import copy

PATH = r"D:\桌面\code\javafx\bilitro\概要设计说明书-模板.docx"
d = Document(PATH)

def find_para(text_start):
    for p in d.paragraphs:
        if p.text.strip().startswith(text_start):
            return p
    raise RuntimeError("not found: " + text_start)

def insert_before(anchor, text, code=False, bold=False):
    p = anchor.insert_paragraph_before("")
    run = p.add_run(text)
    run.font.size = Pt(10.5)
    run.font.bold = bold
    if code:
        run.font.name = "Consolas"
        run._element.rPr.rFonts.set(qn("w:eastAsia"), "Consolas")
    return p

def add_table_after(anchor_para, header, rows):
    tbl = d.add_table(rows=len(rows) + 1, cols=len(header))
    try:
        tbl.style = "Table Grid"
    except KeyError:
        pass  # 模板无此样式，使用默认表格样式
    for j, h in enumerate(header):
        tbl.rows[0].cells[j].text = h
    for i, row in enumerate(rows):
        for j, v in enumerate(row):
            tbl.rows[i + 1].cells[j].text = v
    anchor_para._p.addnext(tbl._tbl)
    return tbl

# ============ 一、项目概述 ============
p1 = find_para("填写本项目完整包结构列表")
p1.text = ""  # 清空占位说明
anchor = find_para("所有标黄")

insert_before(anchor, "本项目采用三层 MVC 架构，完整包结构如下：")
tree = """com.bilitro
├── App.java                        入口：装配 MVC、检测存档、进主菜单
├── model                           模型层：纯 Java，零 JavaFX 依赖
│   ├── GameConfig.java             全局数值常量（手牌数、次数、栏位上限、奖励规则）
│   ├── SaveManager.java            自动存档与最高分记录接口
│   ├── card
│   │   ├── Suit.java               花色枚举
│   │   ├── Rank.java               点数枚举（大小值与计分点数）
│   │   ├── Card.java               扑克牌（不可变 record）
│   │   └── Deck.java               牌组接口：抽牌、剩余查看、按禁用花色重置
│   ├── hand
│   │   ├── HandType.java           牌型枚举（优先级、基础分、基础倍数）
│   │   ├── HandTypeEvaluator.java  牌型判定与出牌合法性校验
│   │   ├── Evaluation.java         判定结果：牌型与参与计分的手牌
│   │   ├── ScoringContext.java     计分中间态，供功能牌读写
│   │   └── ScoreCalculator.java    计分器：得分等于最终分数乘最终倍数
│   ├── player
│   │   ├── Player.java             货币与功能牌栏
│   │   └── SpecialCard.java        功能牌：id、描述、价格、计分触发钩子
│   ├── game
│   │   ├── GameSession.java        对局核心：一局状态、出牌弃牌、结束判定、通关奖励
│   │   ├── LevelRule.java          关卡差异化规则（目标分、禁用花色、提示文案）
│   │   └── GameState.java          状态机：主菜单、回合内、过关、商店、失败、通关
│   └── shop
│       └── Shop.java               随机商品、刷新、购买校验
├── controller                      控制层
│   ├── GameController.java         选牌、出牌、弃牌、查看牌组、查看功能牌
│   ├── ShopController.java         购买、刷新、离开商店
│   └── MenuController.java         新一局、继续游戏、再来一局、设置、退出
└── view                            视图层：JavaFX 全部在此
    ├── GameView.java               对局界面
    ├── ShopView.java               商店界面
    ├── SettlementView.java         结算界面（胜利与失败共用）
    ├── AudioManager.java           音效与背景音乐的开关和播放
    └── components                  复用 UI 组件（卡牌控件、按钮等）"""
for line in tree.splitlines():
    insert_before(anchor, line, code=True)

insert_before(anchor, "")
insert_before(anchor, "每个包的职责说明：", bold=True)
duties = [
    "model：负责游戏规则与数据，包括牌组、牌型判定、计分、关卡状态、商店、存档数据。该层为纯 Java 实现，不依赖任何界面代码，可脱离界面单独进行单元测试。",
    "model.card：扑克牌基础包，包含花色、点数、单张牌与牌组（抽牌堆）。",
    "model.hand：牌型体系包，包含牌型枚举（含优先级、基础分、倍数表）、牌型判定器、计分器及计分过程对象。",
    "model.player：玩家状态包，维护货币与功能牌栏位，定义功能牌。",
    "model.game：对局核心包，负责一局游戏的状态与规则流转，包括出牌、弃牌、结束判定、通关奖励，以及关卡规则与整体状态机。",
    "model.shop：商店包，负责商品随机生成、刷新与购买校验。",
    "controller：控制层，接收视图层事件并翻译为用户意图，调用模型层执行规则，再把新状态交给视图层刷新。该层不写业务规则，也不涉及 JavaFX 控件细节。",
    "view：视图层，负责界面绘制、动画与音效播放。JavaFX 代码只允许出现在这一层。",
]
for t in duties:
    insert_before(anchor, t)

insert_before(anchor, "")
insert_before(anchor, "包依赖关系：", bold=True)
deps = [
    "整体调用方向为单向依赖：view 依赖 controller，controller 依赖 model，反向不允许。用户操作先触发视图层控件事件，由控制层翻译为用户意图后调用模型层执行规则、变更数据，再由控制层把模型层的新状态交给视图层的渲染方法刷新界面。",
    "模型层不依赖视图层与控制层，也不主动通知外界，状态变化统一由控制层中转。",
    "模型层内部依赖关系为：game 依赖 card、hand、player、shop；hand 依赖 card；player 依赖 hand（功能牌的计分钩子）；shop 依赖 player。",
    "视图层只依赖控制层接口与模型层的只读数据快照，控制层依赖模型层接口与视图层接口，三层均可独立替换实现。",
]
for t in deps:
    insert_before(anchor, t)

# ============ 二、核心接口与实体设计 ============
# 2.1 核心实体
p21 = find_para("列出项目核心实体类")
p21.text = ""
anchor22 = find_para("罗列所有自定义核心接口")
add_table_after(p21,
    ["实体类", "核心属性", "作用"],
    [
        ["Card（record）", "Suit suit、Rank rank", "一张扑克牌，不可变"],
        ["Suit（枚举）", "黑桃、红桃、梅花、方块", "花色，用于同花判定与关卡禁用花色"],
        ["Rank（枚举）", "value（大小值，A 为 14）、chips（计分点数）", "点数，用于牌型比较与逐张计分；2 到 10 按面值计分，J/Q/K 计 10，A 计 11"],
        ["HandType（枚举）", "priority（优先级 1 到 9）、baseScore（基础分）、baseMultiplier（基础倍数）", "九种牌型（高牌到同花顺）及其数值表"],
        ["Evaluation（record）", "HandType type、List<Card> scoringCards", "牌型判定结果：最高牌型与参与计分的手牌（按从左往右顺序）"],
        ["ScoringContext", "chips（分数）、mult（倍数）、multMultiplier（倍数乘算修正）", "一次出牌的计分中间态，供功能牌读写，最终得分等于分数乘倍数"],
        ["LevelRule（record）", "targetScore、bannedSuits、hintText", "关卡差异化规则：目标分、禁用花色、进关提示文案"],
        ["GameState（枚举）", "主菜单、回合内、过关、商店、失败、通关", "一局游戏的整体状态机"],
        ["GameConfig", "手牌数 8、选中 1 到 5 张、出牌与弃牌各 4 次、功能牌上限 6、总关数 8、通关奖励 4 代币等常量", "全局数值配置集中管理"],
    ])

# 2.2 接口设计
anchor22.text = ""
anchor23 = find_para("2.3实现设计")
interfaces = [
    ("1. Deck（牌组）", "随机抽牌与剩余牌组查看，对应需求 2.1.3 与 2.2.6。", [
        "List<Card> draw(int n);                    // 随机抽出 n 张，不足则全部抽出",
        "int remaining();                           // 剩余牌数",
        "List<Card> peekRemaining();                // 剩余牌只读快照，供查看牌组界面",
        "void reset(Set<Suit> bannedSuits);         // 重置为完整牌组，可排除禁用花色",
    ]),
    ("2. HandTypeEvaluator（牌型判定器）", "判定选中手牌构成的最高优先级牌型，并校验出牌合法性，对应需求 2.1.1 与 2.1.2。", [
        "Optional<HandType> evaluate(List<Card> selected);         // 判定最高牌型",
        "Optional<Evaluation> evaluateDetail(List<Card> selected); // 牌型与参与计分的手牌",
        "boolean isPlayable(List<Card> selected);                  // 是否可出，决定出牌按钮亮灰",
    ]),
    ("3. ScoreCalculator（计分器）", "按计分流程执行：以牌型基础分和基础倍数初始化，逐张累加手牌点数并触发功能牌，最终得分等于最终分数乘最终倍数，对应需求 2.1.2。", [
        "ScoreBreakdown score(Evaluation eval, List<SpecialCard> specials);",
        "// ScoreBreakdown 包含 baseChips、cardChips、bonusChips、baseMult、finalMult、finalScore",
    ]),
    ("4. SpecialCard（功能牌）", "商店构筑牌，效果以配置表存储，不可升级。", [
        "String id();                               // 配置表唯一 id",
        "String description();                      // 效果描述，供查看浮层",
        "int price();                               // 商店价格",
        "default void onScore(ScoringContext ctx);  // 计分时触发，修改分数与倍数",
    ]),
    ("5. Player（玩家）", "维护货币与功能牌栏。", [
        "int coins();",
        "void addCoins(int delta);",
        "List<SpecialCard> specialCards();",
        "boolean addSpecialCard(SpecialCard card);   // 超过栏位上限返回 false",
        "boolean removeSpecialCard(SpecialCard card);",
    ]),
    ("6. GameSession（对局核心）", "一局游戏的状态与规则流转，是模型层的核心接口。", [
        "int currentLevel();",
        "int remainingTargetScore();",
        "int remainingPlays();",
        "int remainingDiscards();",
        "int totalScore();                       // 本局累计得分，结算与最高分用",
        "List<Card> hand();",
        "Player player();",
        "PlayResult play(List<Card> selected);   // 出牌：计分、扣减目标、补牌、次数减一",
        "void discard(List<Card> selected);      // 弃牌重抽：次数减一、补牌",
        "RoundOutcome outcome();                 // 结束判定：ONGOING、LEVEL_CLEARED、VICTORY、FAILED",
        "int claimLevelClearReward();            // 通关奖励：固定 4 代币加剩余出牌奖励加利息",
        "void advanceLevel(LevelRule rule);      // 进入下一关",
    ]),
    ("7. Shop（商店）", "商品随机生成、刷新与购买校验，对应需求 2.2.1 与 2.2.2。", [
        "List<SpecialCard> goods();               // 当前商品列表",
        "void refresh();                          // 随机刷新",
        "BuyResult buy(SpecialCard item);         // 结果为 SUCCESS、SLOTS_FULL 或 NOT_ENOUGH_COINS",
    ]),
    ("8. SaveManager（存档）", "自动存档与最高分记录，对应需求 2.2.7 与 2.2.4，JSON 格式存储在本地文件。", [
        "void save(GameSession session, int level);",
        "Optional<GameSession> load();",
        "void clearSave();",
        "int loadHighScore();",
        "void saveHighScore(int score);",
    ]),
    ("9. GameController（对局控制）", "接收对局界面事件：选牌、出牌、弃牌、查看牌组、查看功能牌。", [
        "void toggleSelect(Card card);",
        "List<Card> selected();",
        "void onPlay();",
        "void onDiscard();",
        "void onViewDeck();",
        "void onInspectSpecialCard(String specialCardId);",
    ]),
    ("10. ShopController（商店控制）", "接收商店界面事件：购买、刷新、离开商店。", [
        "void onBuy(SpecialCard item);",
        "void onRefresh();",
        "void onLeave();",
    ]),
    ("11. MenuController（菜单与结算控制）", "主菜单与结算界面入口：新一局、继续游戏、再来一局、设置、退出。", [
        "void onNewGame();",
        "void onContinueGame();",
        "void onRestart();",
        "void onOpenSettings();",
        "void onExit();",
        "boolean hasSave();                      // 决定继续游戏按钮亮灰",
    ]),
]
for title, desc, sigs in interfaces:
    insert_before(anchor23, title, bold=True)
    insert_before(anchor23, desc)
    for s in sigs:
        insert_before(anchor23, s, code=True)
    insert_before(anchor23, "")

# 2.3 实现设计
p23 = find_para("对应每个接口，说明配套实现类名称")
p23.text = ""
add_table_after(p23,
    ["接口", "实现类", "职责（仅声明）"],
    [
        ["Deck", "StandardDeck", "标准 52 张牌组，负责洗牌、抽牌、按禁用花色重置"],
        ["HandTypeEvaluator", "DefaultHandTypeEvaluator", "按牌型优先级表从高到低查表判定，返回构成牌型的手牌"],
        ["ScoreCalculator", "DefaultScoreCalculator", "按计分流程逐张累加点数、触发功能牌，产出计分明细"],
        ["SpecialCard", "ConfiguredSpecialCard", "按配置表 id 加载描述与价格，将计分效果委托给效果策略"],
        ["Player", "DefaultPlayer", "维护货币与功能牌栏，执行栏位上限校验"],
        ["GameSession", "DefaultGameSession", "串联判定、计分、补牌、结束判定与奖励结算，管理关卡推进"],
        ["Shop", "RandomShop", "从功能牌池随机抽取商品，按栏位满、货币不足、成功的顺序校验购买"],
        ["SaveManager", "JsonSaveManager", "以 JSON 将对局状态序列化到本地文件，读写最高分"],
        ["GameController", "DefaultGameController", "接收对局界面事件，调用模型层，驱动对局界面刷新与按钮亮灰"],
        ["ShopController", "DefaultShopController", "接收商店界面事件，按购买结果通知商店界面弹出提示"],
        ["MenuController", "DefaultMenuController", "主菜单与结算入口，检测存档，切换场景"],
    ])

d.save(PATH)
print("saved")
