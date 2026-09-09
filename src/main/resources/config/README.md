# 功能牌配置表说明

`special-cards.json` 为功能牌池，当前为空占位，稍后添加对应牌图片和效果文本。

每条功能牌字段：

| 字段 | 含义 |
|---|---|
| id | 唯一标识 |
| name | 名称 |
| description | 效果文本（界面浮层展示） |
| price | 商店价格 |
| image | 牌图片资源路径 |
| effectType | 效果类型：ADD_CHIPS 加分 / ADD_MULT 加倍数 / MULTIPLY_MULT 倍数乘算 |
| effectValue | 效果数值 |

示例：

```json
[
  {
    "id": "bonus_chips",
    "name": "加分芯片",
    "description": "出牌得分额外 +30 分",
    "price": 3,
    "image": "images/special/bonus_chips.png",
    "effectType": "ADD_CHIPS",
    "effectValue": 30
  }
]
```
