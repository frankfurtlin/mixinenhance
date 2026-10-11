# 文档索引

本目录存放 mixinEnhance 的开发文档。面向使用者的说明请看仓库根目录的 [README.md](../README.md)，
这里收录的是**开发 / 维护**相关资料。

| 文档 | 内容 |
| --- | --- |
| [开发规范.md](开发规范.md) | 必须遵守的约定：配置要通用、多语言必须同步、配置项增删流程 |
| [升级指南.md](升级指南.md) | 升级到新版 Minecraft 的标准流程、必查项与踩坑记录 |
| [功能开发实录.md](功能开发实录.md) | 26.3 渲染重构下的开发经验、动态照明实现要点与设计理念 |
| [VSCode使用指南.md](VSCode使用指南.md) | VSCode 任务与调试配置 |

## 外部参考（不在本仓库维护，请直接看最新版）

Mixin / Fabric 的 API 变化很快，**本仓库不再保留任何上游文档副本**：旧副本既会滞后于上游，
也存在许可不兼容（上游 wiki 为 CC BY-NC-SA，与本仓库 All Rights Reserved 冲突）。
需要时直接查上游最新资料：

| 来源 | 用途 |
| --- | --- |
| [Fabric 官方文档 · Mixin](https://docs.fabricmc.net/zh_cn/develop/mixins/accessors)（有中文） | 官方策展文档：访问器、调用器等 |
| [Fabric Wiki · Mixin Examples](https://wiki.fabricmc.net/tutorial:mixin_examples) | 常用写法速查（页面自身标注「正在重写，谨慎参考」） |
| [MixinExtras Wiki](https://github.com/LlamaLad7/MixinExtras/wiki) | **本项目实际在用**（`@ModifyExpressionValue`、`@Local` 等），优先看这里 |
| [SpongePowered Mixin](https://github.com/SpongePowered/Mixin/wiki) | Mixin 本体的机制与 Javadoc |

> 更推荐直接读**本地依赖的源码 / 字节码**，既不滞后也不受许可约束：
> `javap -p -c -classpath <minecraft-merged.jar> <类名>` 能看到原版真实实现与常量，
> 本项目升级/适配时的所有 API 核对都是这么做出来的。
