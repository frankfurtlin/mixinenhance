package com.frankfurtlin.mixinenhance.config.section;

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
    public boolean enableStackRefill = false;                   // 手持物品用完自动补货
    @ConfigEntry.Gui.Tooltip
    public boolean enableAutoSortContainer = false;             // 打开背包/容器时自动整理
    @ConfigEntry.Gui.Tooltip
    public boolean enableFreeCam = false;                       // 自由视角
    @ConfigEntry.Gui.Tooltip
    public boolean enableTotemCounter = false;                  // 图腾计数显示
    @ConfigEntry.Gui.Tooltip
    public boolean enableDynamicLight = false;                  // 动态照明
    @ConfigEntry.Gui.Tooltip
    public boolean enableGamma = false;                         // 亮度调节
    @ConfigEntry.Gui.Tooltip
    public boolean enableNoSlowInFluid = false;                 // 流体中移速不变
    @ConfigEntry.Gui.Tooltip
    public boolean fluidVisible = false;                        // 流体中清澈观察
    @ConfigEntry.Gui.Tooltip
    public boolean noOverlay = false;                           // 视野无遮挡
}
