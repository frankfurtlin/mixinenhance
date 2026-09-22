# VSCode 使用指南 - MixinEnhance Mod 开发

## 🎯 快速开始

### 方式一：使用快捷键（推荐）

#### 运行任务
1. 按 `Ctrl+Shift+P` (或 `F1`)
2. 输入 `Tasks: Run Task`
3. 选择你想要执行的任务：
   - `🎮 运行 Minecraft 客户端` - 启动开发环境
   - `🔨 构建 Mod` - 构建 JAR 文件
   - `📦 构建并安装到 Minecraft` - 一键构建+安装
   - `🧹 清理构建缓存` - 清理 build 目录
   - `🖥️ 运行 Minecraft 服务端` - 启动服务端
   - `🔄 重新构建` - 清理并重新构建

#### 默认任务
- **快速启动**：按 `Ctrl+Shift+B` 会直接运行默认任务（启动 Minecraft 客户端）

---

### 方式二：使用任务面板

1. 打开 VSCode 左侧活动栏的 **任务面板**（图标：📋）
2. 点击 `运行任务` 按钮
3. 从列表中选择任务

---

### 方式三：使用调试功能

#### 调试 Minecraft 客户端
1. 按 `F5` 或点击左侧的 **调试图标**（🐛）
2. 在调试面板顶部选择 `Debug Minecraft Client`
3. 点击绿色播放按钮或按 `F5`

#### 调试 Minecraft 服务端
1. 按 `F5` 或点击左侧的 **调试图标**（🐛）
2. 在调试面板顶部选择 `Debug Minecraft Server`
3. 点击绿色播放按钮或按 `F5`

---

## 📋 任务说明

| 任务图标 | 任务名称 | 功能 | 快捷键 |
|---------|---------|------|--------|
| 🎮 | 运行 Minecraft 客户端 | 启动带 Mod 的 Minecraft 客户端 | `Ctrl+Shift+B` |
| 🔨 | 构建 Mod | 构建 Mod JAR 文件 | - |
| 📦 | 构建并安装到 Minecraft | 构建并自动安装到 %APPDATA%\.minecraft\mods | - |
| 🧹 | 清理构建缓存 | 清理 build 目录和缓存 | - |
| 🖥️ | 运行 Minecraft 服务端 | 启动带 Mod 的服务端 | - |
| 🔄 | 重新构建 | 清理并重新构建 | - |

---

## 🎮 推荐工作流程

### 1. 开发测试
1. 修改代码
2. 按 `Ctrl+Shift+B` 启动 Minecraft 客户端
3. 测试 Mod 功能
4. 关闭游戏，继续修改

### 2. 构建发布
1. 按 `Ctrl+Shift+P`
2. 输入 `Tasks: Run Task`
3. 选择 `🔨 构建 Mod`
4. 构建完成后，JAR 文件在 `build/libs/AMixinEnhance-1.21.5.jar`

### 3. 本地游玩
1. 按 `Ctrl+Shift+P`
2. 输入 `Tasks: Run Task`
3. 选择 `📦 构建并安装到 Minecraft`
4. 启动 Minecraft（Fabric 版本）即可使用

---

## 🛠️ 常见问题

### Q: 运行任务时提示 "gradlew.bat 找不到"？
**A**: 确保你在项目根目录（包含 gradlew.bat 的目录）

### Q: 构建失败，提示 Java 版本错误？
**A**: 确保已安装 JDK 21，并在 VSCode 设置中配置正确的 Java 路径

### Q: 如何查看构建输出？
**A**: 打开 VSCode 的终端面板（`Ctrl+` `），选择任务对应的终端标签

### Q: 如何停止正在运行的任务？
**A**: 
- 打开终端面板
- 找到对应的任务终端
- 按 `Ctrl+C` 或点击垃圾桶图标终止

---

## ⚡ 快捷操作

### 常用快捷键
- **运行默认任务**: `Ctrl+Shift+B`
- **显示所有任务**: `Ctrl+Shift+P` → `Tasks: Run Task`
- **调试**: `F5`
- **停止调试**: `Shift+F5`
- **打开终端**: `Ctrl+` `

### 自定义快捷键（可选）
你可以为常用任务绑定快捷键：

1. 打开 `文件` → `首选项` → `键盘快捷方式` (或按 `Ctrl+K Ctrl+S`)
2. 点击右上角的 `打开键盘快捷方式(JSON)` 图标
3. 添加如下配置：

```json
[
    {
        "key": "f9",
        "command": "workbench.action.tasks.runTask",
        "args": "🎮 运行 Minecraft 客户端"
    },
    {
        "key": "f10",
        "command": "workbench.action.tasks.runTask",
        "args": "🔨 构建 Mod"
    },
    {
        "key": "f11",
        "command": "workbench.action.tasks.runTask",
        "args": "📦 构建并安装到 Minecraft"
    }
]
```

这样你就可以按 F9、F10、F11 快速执行任务了！

---

## 📝 配置说明

### 已配置的功能
- ✅ Java 开发环境
- ✅ Gradle 构建支持
- ✅ Fabric Loom 集成
- ✅ 调试配置
- ✅ 任务自动化
- ✅ 中文编码支持

### 推荐的 VSCode 扩展（已配置）
- **Extension Pack for Java** - Java 开发必备
- **Gradle for Java** - Gradle 支持
- **Fabric Loom** - Fabric Mod 开发
- **GitLens** - Git 增强

打开项目时会自动提示安装推荐的扩展。

---

## 🎯 快速测试配置

确保一切正常工作：

1. 打开 VSCode 终端（`Ctrl+` `
2. 输入：`gradlew.bat --version`
3. 应该显示 Gradle 版本信息
4. 按 `Ctrl+Shift+B`
5. 应该开始启动 Minecraft

如果以上都正常，说明配置成功！

---

## 📚 更多信息

- **Fabric Wiki**: https://fabricmc.net/wiki/
- **Mixin 文档**: 查看项目中的 `doc/mixin.md`
- **项目文档**: 查看 `使用说明.txt`

---

**祝你开发愉快！** 🎉
