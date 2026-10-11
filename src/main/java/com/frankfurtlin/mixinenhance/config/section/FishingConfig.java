package com.frankfurtlin.mixinenhance.config.section;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * 钓鱼：自动钓鱼、急速钓鱼，以及钓鱼战利品按种类过滤。
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public class FishingConfig {
    public static class LootFilterConfig {
        // 鱼
        @ConfigEntry.Gui.Tooltip
        public boolean excludeCod = false;              // 排除：鳕鱼
        @ConfigEntry.Gui.Tooltip
        public boolean excludeSalmon = false;           // 排除：鲑鱼
        @ConfigEntry.Gui.Tooltip
        public boolean excludeTropicalFish = false;     // 排除：热带鱼
        @ConfigEntry.Gui.Tooltip
        public boolean excludePufferfish = false;       // 排除：河豚
        // 垃圾
        @ConfigEntry.Gui.Tooltip
        public boolean excludeBamboo = false;           // 排除：竹子
        @ConfigEntry.Gui.Tooltip
        public boolean excludeBowl = false;             // 排除：碗
        @ConfigEntry.Gui.Tooltip
        public boolean excludeLeather = false;          // 排除：皮革
        @ConfigEntry.Gui.Tooltip
        public boolean excludeLeatherBoots = false;     // 排除：皮革靴子
        @ConfigEntry.Gui.Tooltip
        public boolean excludeFishingRod = false;       // 排除：钓鱼竿（含附魔）
        @ConfigEntry.Gui.Tooltip
        public boolean excludeString = false;           // 排除：线
        @ConfigEntry.Gui.Tooltip
        public boolean excludeStick = false;            // 排除：木棍
        @ConfigEntry.Gui.Tooltip
        public boolean excludePotion = false;           // 排除：水瓶
        @ConfigEntry.Gui.Tooltip
        public boolean excludeInkSac = false;           // 排除：墨囊
        @ConfigEntry.Gui.Tooltip
        public boolean excludeRottenFlesh = false;      // 排除：腐肉
        @ConfigEntry.Gui.Tooltip
        public boolean excludeBone = false;             // 排除：骨头
        @ConfigEntry.Gui.Tooltip
        public boolean excludeTripwireHook = false;     // 排除：绊线钩
        // 宝藏
        @ConfigEntry.Gui.Tooltip
        public boolean excludeEnchantedBook = false;    // 排除：附魔书
        @ConfigEntry.Gui.Tooltip
        public boolean excludeNameTag = false;          // 排除：命名牌
        @ConfigEntry.Gui.Tooltip
        public boolean excludeNautilusShell = false;    // 排除：鹦鹉螺壳
        @ConfigEntry.Gui.Tooltip
        public boolean excludeSaddle = false;           // 排除：鞍
        @ConfigEntry.Gui.Tooltip
        public boolean excludeBow = false;              // 排除：弓（含附魔）
    }

    @ConfigEntry.Gui.Tooltip
    public boolean enableAutoFishing = false;           // 自动钓鱼
    @ConfigEntry.Gui.Tooltip
    public boolean enableTickFishing = false;           // 急速钓鱼
    @ConfigEntry.Gui.Tooltip
    public boolean enableExpOnlyMode = false;           // 钓鱼经验模式：只给经验，不给战利品

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public LootFilterConfig lootFilterConfig = new LootFilterConfig();
}
