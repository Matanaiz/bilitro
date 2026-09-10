# -*- coding: utf-8 -*-
"""生成主状态机图与子状态机图 PNG。"""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(sys.executable).parent.parent.parent))
from daimon_runtime import setup_plot
setup_plot()

import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch

WS = Path(r"D:\桌面\code\javafx\bilitro")


def box(ax, x, y, text, w=1.9, h=0.62):
    ax.add_patch(FancyBboxPatch((x - w / 2, y - h / 2), w, h,
                                boxstyle="round,pad=0.06",
                                fc="#E8F0FE", ec="#3A5FCD", lw=1.4))
    ax.text(x, y, text, ha="center", va="center", fontsize=11)


def arrow(ax, p1, p2, label, rad=0.0, lx=0.0, ly=0.14):
    ax.annotate("", xy=p2, xytext=p1,
                arrowprops=dict(arrowstyle="-|>", color="#444444", lw=1.3,
                                connectionstyle=f"arc3,rad={rad}"))
    mx, my = (p1[0] + p2[0]) / 2 + lx, (p1[1] + p2[1]) / 2 + ly
    ax.text(mx, my, label, ha="center", va="center", fontsize=9,
            color="#333333",
            bbox=dict(fc="white", ec="none", alpha=0.85, pad=1))


# ============ 主状态机图 ============
fig, ax = plt.subplots(figsize=(11, 7))
ax.set_xlim(0, 10)
ax.set_ylim(0, 7)
ax.axis("off")

P = {
    "MAIN_MENU": (1.4, 5.6),
    "IN_ROUND": (4.2, 5.6),
    "PAUSED": (7.2, 5.6),
    "LEVEL_CLEAR": (4.2, 3.9),
    "SHOPPING": (7.2, 3.9),
    "VICTORY": (2.4, 2.0),
    "FAILED": (6.0, 2.0),
}
names = {
    "MAIN_MENU": "MAIN_MENU\n主菜单",
    "IN_ROUND": "IN_ROUND\n回合内",
    "PAUSED": "PAUSED\n暂停",
    "LEVEL_CLEAR": "LEVEL_CLEAR\n过关",
    "SHOPPING": "SHOPPING\n商店",
    "VICTORY": "VICTORY\n通关结算",
    "FAILED": "FAILED\n失败结算",
}
for k, (x, y) in P.items():
    box(ax, x, y, names[k])

arrow(ax, (2.35, 5.75), (3.25, 5.75), "新一局 / 继续游戏")
arrow(ax, (5.15, 5.75), (6.25, 5.75), "暂停")
arrow(ax, (6.25, 5.45), (5.15, 5.45), "继续", ly=-0.16)
arrow(ax, (7.2, 5.95), (1.4, 6.3), "回主菜单（先自动存档）", rad=0.25, ly=0.2)
arrow(ax, (4.2, 5.28), (4.2, 4.25), "达标且非第 8 关", lx=1.35)
arrow(ax, (5.15, 3.9), (6.25, 3.9), "领取通关奖励", ly=0.28)
arrow(ax, (7.5, 4.25), (4.7, 5.3), "离开商店，进入下一关", rad=-0.2, lx=1.1, ly=0.1)
arrow(ax, (3.8, 5.28), (2.6, 2.35), "第 8 关达标", rad=0.12, lx=-0.9)
arrow(ax, (4.6, 5.28), (5.8, 2.35), "次数耗尽未达标", rad=-0.12, lx=1.7, ly=-0.55)
arrow(ax, (2.4, 1.68), (3.9, 5.25), "再来一局", rad=0.35, lx=-1.0, ly=0.0)
arrow(ax, (6.0, 1.68), (4.5, 5.25), "再来一局", rad=-0.3, lx=1.0, ly=0.0)

fig.savefig(WS / "state_main.png", bbox_inches="tight", dpi=160)
plt.close(fig)

# ============ 子状态机图（父状态 IN_ROUND） ============
fig, ax = plt.subplots(figsize=(10, 5.2))
ax.set_xlim(0, 10)
ax.set_ylim(0, 5.2)
ax.axis("off")

Q = {
    "选牌中": (1.8, 3.6),
    "计分动画中": (8.2, 3.6),
    "补牌结算": (5.0, 1.1),
}
for k, (x, y) in Q.items():
    box(ax, x, y, k, w=2.0, h=0.7)

arrow(ax, (2.85, 3.6), (7.15, 3.6), "点击出牌（选中 1 到 5 张且剩余出牌次数大于 0）", ly=0.35)
arrow(ax, (7.6, 3.3), (5.7, 1.45), "动画播完，落账得分", rad=0.15, lx=1.3)
arrow(ax, (4.3, 1.45), (2.4, 3.3), "补牌完成，解锁操作", rad=0.15, lx=-1.3)
arrow(ax, (2.6, 3.35), (4.35, 1.35), "点击弃牌（剩余弃牌次数大于 0）", rad=-0.25, lx=-0.4, ly=0.5)
ax.text(5.0, 0.25, "父状态：IN_ROUND（回合内）", ha="center", fontsize=10, color="#555555")

fig.savefig(WS / "state_sub.png", bbox_inches="tight", dpi=160)
plt.close(fig)
print("ok")
