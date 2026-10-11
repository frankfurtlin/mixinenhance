package com.frankfurtlin.mixinenhance.config.section;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * 方块与挖掘：破坏/采集类方块特性，以及相关特殊方块（刷怪笼、宝库、精准采集）。
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public class BlockConfig {
    public static class SilkTouchConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean enableSpawnerDropWithSilkTouch = false;          // 精准采集可破坏刷怪笼
        @ConfigEntry.Gui.Tooltip
        public boolean enableTrialSpawnerDropWithSilkTouch = false;     // 精准采集可破坏试炼刷怪笼
        @ConfigEntry.Gui.Tooltip
        public boolean enableVaultDropWithSilkTouch = false;            // 精准采集可破坏宝库
        @ConfigEntry.Gui.Tooltip
        public boolean enableBuddingAmethystDropWithSilkTouch = false;  // 精准采集可破坏紫水晶母岩
        @ConfigEntry.Gui.Tooltip
        public boolean enableFarmlandDropWithSilkTouch = false;         // 精准采集可破坏耕地
        @ConfigEntry.Gui.Tooltip
        public boolean enableSuspiciousSandDropWithSilkTouch = false;   // 精准采集可破坏可疑的沙子
        @ConfigEntry.Gui.Tooltip
        public boolean enableSuspiciousGravelDropWithSilkTouch = false; // 精准采集可破坏可疑的沙砾
    }

    public static class SpawnerFarmConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean enableSpawnerFarm = false;                        // 刷怪笼秒变刷怪塔（六面任一相邻位置红石块激活）
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int spawnerFarmRate = 1;                                  // 刷怪塔倍率（产出频率）
    }

    public static class TrialConfig {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 30)
        public int trialSpawnerCoolDown = 30;                            // 试炼刷怪笼冷却，单位分钟
        @ConfigEntry.Gui.Tooltip
        public boolean unLockVaultReward = false;                        // 宝库无限兑换
    }

    @ConfigEntry.Gui.Tooltip
    public boolean enableFallingTree = false;                   // 砍树时整棵树倒下
    @ConfigEntry.Gui.Tooltip
    public boolean enableVeinMining = false;                    // 连锁挖矿
    @ConfigEntry.Gui.Tooltip
    public boolean enableFastLeafDecay = false;                 // 树叶快速凋零
    @ConfigEntry.Gui.Tooltip
    public boolean enableFarmlandNoLandToDirt = false;          // 耕地践踏不转化成泥土
    @ConfigEntry.Gui.Tooltip
    public boolean enableFrostWalkerWorkOnLava = false;         // 冰霜行者将熔岩变成霜冰

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public SilkTouchConfig silkTouchConfig = new SilkTouchConfig();
    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public SpawnerFarmConfig spawnerFarmConfig = new SpawnerFarmConfig();
    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public TrialConfig trialConfig = new TrialConfig();
}
