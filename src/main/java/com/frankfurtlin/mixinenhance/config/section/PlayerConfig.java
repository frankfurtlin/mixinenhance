package com.frankfurtlin.mixinenhance.config.section;

import com.frankfurtlin.mixinenhance.config.HotkeyHint;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * 玩家：与玩家自身操控/感知（移动、视角、显示、背包）相关的开关。
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public class PlayerConfig {
    @ConfigEntry.Gui.Tooltip
    public boolean enablePlayerExpPickUpNoDelay = false;        // 玩家吸收经验无冷却
    @ConfigEntry.Gui.Tooltip
    public boolean enableXpMerge = true;                        // 经验球合并（8 格内同值经验球并成一个）
    @ConfigEntry.Gui.Tooltip
    public boolean enableStackRefill = false;                   // 手持物品用完自动补货
    @ConfigEntry.Gui.Tooltip
    public boolean enableAutoSortContainer = false;             // 打开背包/容器时自动整理
    @ConfigEntry.Gui.Tooltip
    @HotkeyHint("freecam")
    public boolean enableFreeCam = true;                        // 自由视角（游戏内 F6 切换；关掉则按 F6 无反应）
    @ConfigEntry.Gui.Tooltip
    public boolean enableTotemCounter = false;                  // 图腾计数显示
    @ConfigEntry.Gui.Tooltip
    public boolean enableDynamicLight = false;                  // 动态照明
    @ConfigEntry.Gui.Tooltip
    @HotkeyHint("gamma")
    public boolean enableGamma = true;                          // 亮度调节（默认原版亮度，游戏内按 F7 切全亮；关掉配置则按 F7 无反应）
    @ConfigEntry.Gui.Tooltip
    public boolean enableNoSlowInFluid = false;                 // 流体中移速不变
    @ConfigEntry.Gui.Tooltip
    public boolean fluidVisible = false;                        // 流体中清澈观察
    @ConfigEntry.Gui.Tooltip
    public boolean noOverlay = false;                           // 视野无遮挡
}
