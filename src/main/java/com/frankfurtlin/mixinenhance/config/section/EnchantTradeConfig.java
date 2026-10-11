package com.frankfurtlin.mixinenhance.config.section;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * 附魔与交易：附魔冲突/铁砧上限，以及村民交易解锁。
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public class EnchantTradeConfig {
    public static class EnchantmentConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean removeAnvilLimit = false;                            // 消除附魔经验上限
        @ConfigEntry.Gui.Tooltip
        public boolean removeDamageEnchantmentConflict = true;              // 锋利/亡灵杀手/节肢杀手/破甲/致密 不冲突
        @ConfigEntry.Gui.Tooltip
        public boolean removeProtectionEnchantmentConflict = true;          // 保护/爆炸保护/弹射物保护/火焰保护 不冲突
        @ConfigEntry.Gui.Tooltip
        public boolean removeBowEnchantmentConflict = true;                 // 弓：经验修补/无限 不冲突
        @ConfigEntry.Gui.Tooltip
        public boolean removeCrossbowEnchantmentConflict = true;            // 弩：多重射击/穿透 不冲突
        @ConfigEntry.Gui.Tooltip
        public boolean removeBootEnchantmentConflict = true;                // 靴子：深海探索者/冰霜行者 不冲突
    }

    public static class TradeConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean unLockTrade = false;                                 // 无限交易
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
        public int tradeCount = 2;                                          // 每级村民解锁的交易数
    }

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public EnchantmentConfig enchantmentConfig = new EnchantmentConfig();
    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public TradeConfig tradeConfig = new TradeConfig();
}
