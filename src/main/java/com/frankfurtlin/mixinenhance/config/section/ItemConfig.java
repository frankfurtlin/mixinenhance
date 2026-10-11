package com.frankfurtlin.mixinenhance.config.section;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * 物品与装备：物品属性、堆叠、以及各类以物品形式存在的效果来源。
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public class ItemConfig {
    public static class SpongeConfig {
        @ConfigEntry.Gui.Tooltip
        public boolean canAbsorbLava = false;                           // 海绵可吸附岩浆
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 32)
        public int maxDepth = 6;                                        // 海绵最大吸附深度
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 32, max = 32768)
        public int maxIterations = 65;                                  // 海绵最大吸附方块数量
    }

    public static class ExplosionConfig {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 128)
        public int tntExplodeRadius = 4;                                // TNT 爆炸范围
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 128)
        public int endCrystalExplodeRadius = 6;                         // 末影水晶爆炸范围
    }

    public static class ProjectileDamageConfig {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 15)
        public int smallFireballDamage = 5;                             // 小火球伤害（火焰弹、烈焰人）
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
        public int fireballDamage = 6;                                  // 大火球伤害（恶魂）
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
        public int snowballDamage = 0;                                  // 雪球伤害
    }

    @ConfigEntry.Gui.Tooltip
    public boolean canStackTo64 = false;                        // 部分物品堆叠至64
    @ConfigEntry.Gui.Tooltip
    public boolean inventoryTotemEnabled = false;               // 不死图腾背包中生效
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = -1, max = 3600)
    public int spectralArrowDuration = 10;                      // 光灵箭荧光持续时间（秒）
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int experienceBottleValue = 5;                       // 经验瓶经验值

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public SpongeConfig spongeConfig = new SpongeConfig();
    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public ExplosionConfig explosionConfig = new ExplosionConfig();
    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public ProjectileDamageConfig projectileDamageConfig = new ProjectileDamageConfig();
}
