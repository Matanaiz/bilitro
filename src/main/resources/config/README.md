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
| growthType | 成长时机（可省略）：NONE 不成长 / PER_HAND 每打出一次牌积累一次 / PER_DISCARD 每弃掉一张指定花色的牌积累一次 |
| growthValue | 每次积累量（可省略）；效果数值 = effectValue + 已积累次数 × growthValue |
| suitMode | 随机花色方式（可省略）：PERMANENT 首次随机后永久固定 / PER_LEVEL 每关重新随机；conditionValue 需为 "RANDOM" |
| discardsDelta | 被动：每关弃牌次数修正（可省略，可为负） |
| handSizeDelta | 被动：手牌上限修正（可省略，可为负） |
| creditLimit | 被动：商店可负债额度（可省略） |
| levelClearCoins | 被动：每关通关时额外获得的代币（可省略） |

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
| SCORING_SUIT | 逐张计分时（当前牌为指定花色） | 花色枚举名，如 HEART；"RANDOM" 表示随机花色（配合 suitMode） |
| SCORING_FACE | 逐张计分时（当前牌为人头牌 J/Q/K） | 无 |
| SCORING_RANKS | 逐张计分时（当前牌点数在列表中） | 逗号分隔点数，如 ACE,TWO,THREE,FIVE,EIGHT |
| RETRIGGER_FACE | 人头牌额外计分 value 次 | 无 |
| RETRIGGER_RANKS | 列表中的点数额外计分 value 次 | 逗号分隔点数 |
| META_FACE | 持有期间所有手牌均视为人头牌 | 无 |
| META_ALL_SCORE | 所有打出的牌都参与计分（"飞溅"） | 无 |
| META_MERGE_SUITS | 红桃=方块、梅花=黑桃（"模糊小丑"，影响同花判定） | 无 |
| HAND_ALL_SUITS | 打出的牌包含全部四种花色时触发（整手一次，"花盆"） | 无 |

说明：重触发类（RETRIGGER_*）与元规则类（META_*）不通过 effectType 修改分数，effectType/effectValue 字段中 effectValue 对重触发表示额外计分次数。

成长类示例（每打出一次永久 +50 积分）：

```json
{
  "id": "growing_chips",
  "name": "积累筹码",
  "description": "计分时给予本牌已积累的积分",
  "price": 5,
  "image": "",
  "effectType": "ADD_CHIPS",
  "effectValue": 0,
  "conditionType": "ALWAYS",
  "conditionValue": null,
  "growthType": "PER_HAND",
  "growthValue": 50
}
```
