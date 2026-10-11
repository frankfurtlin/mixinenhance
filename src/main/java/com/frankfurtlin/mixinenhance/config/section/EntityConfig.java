package com.frankfurtlin.mixinenhance.config.section;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * 实体与怪物：怪物生成时的装备系数，以及各类敌对生物的行为调整。
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public class EntityConfig {
    public static class MobConfig {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public double dieWithSpawner = 0.01;                        // 怪物死亡时掉落对应刷怪笼的概率
    }

    /**
     * 怪物生成时的装备系数（0-1）：一个系数同时决定「有没有装备、材质好不好、附不附魔、附魔多强、
     * 死亡时掉不掉落」，全部只取决于配置，不再受区域难度/月相/游戏天数影响。
     */
    public static class EquipmentConfig {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public double armorFactor = 0.15;                           // 怪物盔甲系数（概率/材质/附魔/掉落）
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public double weaponFactor = 0.15;                          // 怪物武器系数（概率/材质/附魔/掉落）
    }

    public static class HostileMobConfig {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public float pickupLootChance = 0.55f;                      // 僵尸拾取物品的概率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 5)
        public int drownedSpawnFactor = 1;                          // 溺尸生成倍率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int ghastSpawnFactor = 1;                            // 恶魂生成倍率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int creeperExplodeRadius = 3;                        // 苦力怕爆炸半径
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public float spiderSpawnWithEffect = 0.1f;                  // 蜘蛛生成时带有效果的概率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public float witherSkeletonSkullDropRate = 0.01f;           // 凋零骷髅死亡时掉落头颅的概率
    }

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public MobConfig mobConfig = new MobConfig();
    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public EquipmentConfig equipmentConfig = new EquipmentConfig();
    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public HostileMobConfig hostileMobConfig = new HostileMobConfig();
}
