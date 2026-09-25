package com.frankfurtlin.mixinenhance.config.modMenuMuduleConfig;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 7:10
 */
public class BlockModuleConfig {
    public static class SilkTouchConfig{
        @ConfigEntry.Gui.Tooltip
        public boolean enableSpawnerDropWithSilkTouch = false;       // 是否启用刷怪笼被精准采集掉落
        @ConfigEntry.Gui.Tooltip
        public boolean enableTrialSpawnerDropWithSilkTouch = false;       // 是否启用试炼刷怪笼被精准采集掉落
        @ConfigEntry.Gui.Tooltip
        public boolean enableVaultDropWithSilkTouch = false;       // 是否启用宝库被精准采集掉落
        @ConfigEntry.Gui.Tooltip
        public boolean enableBuddingAmethystDropWithSilkTouch = false;       // 是否启用紫水晶母岩被精准采集掉落
        @ConfigEntry.Gui.Tooltip
        public boolean enableFarmlandDropWithSilkTouch = false;       // 是否启用耕地被精准采集掉落
        @ConfigEntry.Gui.Tooltip
        public boolean enableSuspiciousSandDropWithSilkTouch = false;       // 是否启用可疑沙子被精准采集掉落
        @ConfigEntry.Gui.Tooltip
        public boolean enableSuspiciousGravelDropWithSilkTouch = false;       // 是否启用可疑沙砾被精准采集掉落
    }

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public BlockModuleConfig.SilkTouchConfig silkTouchConfig = new BlockModuleConfig.SilkTouchConfig();

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 30)
    public int trialSpawnerCoolDown = 30;                            // 试炼刷怪笼的冷却时间，单位分钟
    @ConfigEntry.Gui.Tooltip
    public boolean unLockVaultReward = false;                        // 是否启用宝库无限兑换
    @ConfigEntry.Gui.Tooltip
    public boolean enableFallingTree = false;                        // 是否启用砍树时整棵树一起倒下
    @ConfigEntry.Gui.Tooltip
    public boolean enableVeinMining = false;                         // 是否启用挖掘矿石时连锁挖掉相连的同种矿石
}
