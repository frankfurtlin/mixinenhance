package com.frankfurtlin.mixinenhance.config.modMenuMuduleConfig;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 7:09
 */
public class DefaultModuleConfig {
    public static class EnchantmentConfig{
        @ConfigEntry.Gui.Tooltip
        public boolean removeAnvilLimit = false;                     // 消除附魔经验上限
        @ConfigEntry.Gui.Tooltip
        public boolean removeDamageEnchantmentConflict = true;       // 是否允许锋利、亡灵杀手、节肢杀手、破甲、致密不冲突
        @ConfigEntry.Gui.Tooltip
        public boolean removeProtectionEnchantmentConflict = true;   // 是否允许保护、爆炸保护、弹射物保护、火焰保护不冲突
        @ConfigEntry.Gui.Tooltip
        public boolean removeBowEnchantmentConflict = true;          // 是否允许弓经验修补、无限不冲突
        @ConfigEntry.Gui.Tooltip
        public boolean removeCrossbowEnchantmentConflict = true;     // 是否允许弩多重射击、穿透不冲突
        @ConfigEntry.Gui.Tooltip
        public boolean removeBootEnchantmentConflict = true;         // 是否允许靴子深海探索者、冰霜行者不冲突
    }

    @ConfigEntry.Gui.Tooltip
    public boolean keepInventory = true;                        // 是否启用死亡不掉落
    @ConfigEntry.Gui.Tooltip
    public boolean enableAutoFishing = false;                   // 是否启用自动钓鱼
    @ConfigEntry.Gui.Tooltip
    public boolean enableTickFishing = false;                   // 是否启用急速钓鱼
    @ConfigEntry.Gui.Tooltip
    public boolean unLockTrade = false;                         // 是否无限交易
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
    public int tradeCount = 2;                                  // 每一等级村民解锁的交易选项数
    @ConfigEntry.Gui.Tooltip
    public boolean enablePlayerExpPickUpNoDelay = false;        // 是否启用玩家吸收经验无冷却
    @ConfigEntry.Gui.Tooltip
    public boolean enableJoinWithItems = false;                 // 是否启用玩家首次进入世界时赠送初始物资
    @ConfigEntry.Gui.Tooltip
    public boolean enableFrostWalkerWorkOnLava = false;         // 是否启用冰霜行者将熔岩变成霜冰
    @ConfigEntry.Gui.Tooltip
    public boolean enableSpawnerFarm = false;                   // 是否启用刷怪笼农场
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
    public int spawnerFarmRate = 1;                             // 刷怪笼农场倍率
    @ConfigEntry.Gui.Tooltip
    public boolean removeExperimentalWarning = true;            // 是否消除加载世界时的实验性特性弹窗
    @ConfigEntry.Gui.Tooltip
    public boolean canOpenGuiInPortal = false;                  // 玩家在地狱门中不强制关闭背包页面
    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public DefaultModuleConfig.EnchantmentConfig enchantmentConfig = new DefaultModuleConfig.EnchantmentConfig();

}
