# MixinEnhance · 原版游戏增强

[![Build](https://github.com/Frankfurtlin/mixinEnhance/actions/workflows/build.yml/badge.svg)](https://github.com/Frankfurtlin/mixinEnhance/actions/workflows/build.yml)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.3-3c8527)](https://www.minecraft.net/)
[![Loader](https://img.shields.io/badge/Loader-Fabric-dbb69b)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-e76f00)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red)](#-许可证)

一套面向 **Minecraft 26.3 / Fabric** 的原版向增强模组：在尽量贴近原版体验的前提下，补齐一批
"原版能做到但很繁琐、或干脆做不到"的实用功能（连锁砍挖、树叶快速凋零、容器自动整理、附魔与
交易优化、怪物/钓鱼调整等）。所有功能均可在游戏内单独开关。

> 游戏内显示名称为 `AAAMixinenhance`（便于在模组列表里排在最前），仓库/模组 id 为 `mixinenhance`。

## ✨ 特性

配置在 **Mod Menu → 原版游戏增强 → 配置** 中，按功能域分为 7 个标签页：

### 常规
- 死亡不掉落
- 消除加载世界时的实验性特性弹窗
- 玩家在地狱门中不强制关闭背包
- 首次进入世界时赠送初始物资
- 灵魂沙 / 蜂蜜块上移动不减速

### 玩家
- 吸收经验无冷却
- 手持物品用完自动补货
- **打开背包 / 容器自动整理**（合并相同物品并按物品顺序排序；容器只整理自身存储格，背包界面只整理主背包、不含快捷栏）
- 自由视角（按键切换，身体留在原地）
- 图腾计数显示（经验条上方显示背包与副手中的不死图腾数量）
- 动态照明（手持发光物照亮周围）
- 亮度调节
- 流体中移速不变 / 流体中清澈观察 / 视野无遮挡

### 方块与挖掘
- 砍树时整棵树倒下、连锁挖矿
- **树叶快速凋零**（砍树后相连的自然树叶立即整体凋零，玩家放置的树叶不受影响）
- 耕地践踏不转化成泥土、冰霜行者把熔岩变成霜冰
- 精准采集可破坏：刷怪笼 / 试炼刷怪笼 / 宝库 / 紫水晶母岩 / 耕地 / 可疑的沙 / 可疑的沙砾
- 刷怪笼秒变刷怪塔（倍率可调）、试炼刷怪笼冷却时间可调、宝库无限兑换

### 物品与装备
- 部分物品堆叠至 64
- 不死图腾在背包中生效
- 光灵箭荧光持续时间、经验瓶经验值（可调）
- 海绵可吸附岩浆（深度 / 数量可调）
- TNT / 末影水晶爆炸范围（可调）
- 小火球 / 大火球 / 雪球伤害（可调）

### 实体与怪物
- 怪物血量 / 攻击倍率
- 怪物生成时自带装备与附魔概率、盔甲强度增强、死亡掉落刷怪笼概率
- 僵尸 / 溺尸 / 恶魂 / 猪灵 / 苦力怕 / 蜘蛛 / 凋零骷髅 等生成与行为调整

### 附魔与交易
- 消除附魔经验上限
- 去除附魔冲突（伤害类 / 保护类 / 弓 / 弩 / 靴子）
- 无限交易、每级村民解锁交易数（可调）

### 钓鱼
- 自动钓鱼、急速钓鱼
- 钓鱼战利品逐项过滤（鱼 / 垃圾 / 宝藏共 21 种，可单独排除）

## 📦 安装

1. 安装 **Fabric Loader 0.19.5+**，并确保使用 **Java 25**。
2. 安装前置 **[Fabric API](https://modrinth.com/mod/fabric-api)**。
3. （可选，用于图形化配置）安装 **[Mod Menu](https://modrinth.com/mod/modmenu)** 与 **[Cloth Config](https://modrinth.com/mod/cloth-config)**。
4. 从本仓库的 **[Releases](https://github.com/Frankfurtlin/mixinEnhance/releases)** 下载最新 jar，放入 `.minecraft/mods`。

## 🔧 配置

进入游戏后 **Mod Menu → 原版游戏增强 → 配置**，按 7 个标签页逐项开关。所有功能默认关闭（
死亡不掉落等少数默认开启），按需启用即可。修改后即时生效（少数项需重进世界）。

## 🛠 从源码构建

需要 **JDK 25**。

```bash
./gradlew build
```

构建产物位于 `build/libs/AMixinEnhance-26.3.jar`（另有一个 `-sources.jar` 为源码包）。

> 开发时可运行 `./gradlew runClient` 启动开发客户端进行测试。

## ⌨ 快捷键

| 按键 | 功能 |
| --- | --- |
| `F4` | 切换自由视角 |

## 📄 许可证

**All Rights Reserved（保留所有权利）。** 详见 [LICENSE](LICENSE)。

未经作者书面许可，不得复制、分发、修改或用于商业用途；仅可从官方渠道获取官方构建用于个人非商业用途。

## 🔗 链接

- 作者主页：https://frankfurtlin.top
- 仓库：https://github.com/Frankfurtlin/mixinEnhance
