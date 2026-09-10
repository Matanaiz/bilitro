# 功能牌配置表说明

`special-cards.json` 为功能牌池，每条功能牌字段：

| 字段 | 含义 |
|---|---|
| id | 唯一标识 |
| name | 名称 |
| description | 效果文本（界面浮层展示） |
| price | 商店价格 |
| image | 牌图片资源路径（暂为空占位） |
| effectType | 效果类型 |
| effectValue | 效果数值 |
| conditionType | 触发条件 |
| conditionValue | 条件参数 |

effectType 取值：

| 值 | 含义 |
|---|---|
| ADD_CHIPS | 积分 += value |
| ADD_MULT | 倍数 += value |
| MULTIPLY_MULT | 倍数 *= value |
| ADD_MULT_PER_SPECIAL | 倍数 += value × 持有功能牌数量 |

conditionType 取值：

| 值 | 触发时机 | conditionValue |
|---|---|---|
| ALWAYS | 整手结算一次 | 无 |
| HAND_TYPE | 整手结算一次（牌型"包含"指定牌型时） | 牌型枚举名，如 PAIR |
| SCORING_SUIT | 逐张计分时（当前牌为指定花色） | 花色枚举名，如 HEART |
| SCORING_FACE | 逐张计分时（当前牌为人头牌 J/Q/K） | 无 |
| SCORING_RANKS | 逐张计分时（当前牌点数在列表中） | 逗号分隔点数，如 ACE,TWO,THREE,FIVE,EIGHT |
| RETRIGGER_FACE | 人头牌额外计分 value 次 | 无 |
| RETRIGGER_RANKS | 列表中的点数额外计分 value 次 | 逗号分隔点数 |
| META_FACE | 持有期间所有手牌均视为人头牌 | 无 |

说明：重触发类（RETRIGGER_*）与元规则类（META_FACE）不通过 effectType 修改分数，effectType/effectValue 字段中 effectValue 对重触发表示额外计分次数。
