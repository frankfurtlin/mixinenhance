package com.frankfurtlin.mixinenhance.config.section;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * 常规：通用规则类开关，与具体玩法模块无关。
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public class GeneralConfig {
    @ConfigEntry.Gui.Tooltip
    public boolean keepInventory = true;                        // 死亡不掉落
    @ConfigEntry.Gui.Tooltip
    public boolean removeExperimentalWarning = true;            // 消除加载世界时的实验性特性弹窗
    @ConfigEntry.Gui.Tooltip
    public boolean canOpenGuiInPortal = false;                  // 玩家在地狱门中不强制关闭背包页面
    @ConfigEntry.Gui.Tooltip
    public boolean enableJoinWithItems = false;                 // 玩家首次进入世界时赠送初始物资
    @ConfigEntry.Gui.Tooltip
    public boolean noSlowOnSoulSand = false;                    // 玩家在灵魂沙上不减速
    @ConfigEntry.Gui.Tooltip
    public boolean noSlowOnHoneyBlock = false;                  // 玩家在蜂蜜块上不减速
}
