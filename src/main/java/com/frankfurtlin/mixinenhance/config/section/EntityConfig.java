package com.frankfurtlin.mixinenhance.config.section;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * 实体与怪物：怪物属性倍率、生成时装备/附魔，以及各类敌对生物的行为调整。
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public class EntityConfig {
    public static class MobConfig {
        @ConfigEntry.Gui.Tooltip
        public double healthMultiplier = 1.0;                       // 怪物血量倍率
        @ConfigEntry.Gui.Tooltip
        public double attackMultiplier = 1.0;                       // 怪物攻击倍率（近战与远程伤害）
        @ConfigEntry.Gui.Tooltip
        public boolean enableMobArmorEnhancement = false;           // 怪物生成时盔甲强度增强
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public float spawnEquipmentChance = 0.15f;                  // 怪物生成时自带盔甲的概率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public float enchantmentArmorChance = 0.5f;                 // 怪物生成时盔甲附魔的概率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public float enchantmentMainHandChance = 0.25f;             // 怪物生成时主手工具附魔的概率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public float armorAndHandDropChance = 0.085f;               // 怪物死亡时工具及盔甲掉落的概率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public double dieWithSpawner = 0.01;                        // 怪物死亡时掉落对应刷怪笼的概率
    }

    public static class HostileMobConfig {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public float pickupLootChance = 0.55f;                      // 僵尸拾取物品的概率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public double zombieSpawnWithTool = 0.05;                   // 僵尸生成时自带武器的概率
        @ConfigEntry.Gui.Tooltip
        public boolean enableZombieWeaponEnhancement = false;       // 僵尸武器升级（所有种类）
        @ConfigEntry.Gui.Tooltip
        public boolean enableVindicatorWeaponEnhancement = false;   // 卫道士武器升级（铁、钻石、下界合金剑）
        @ConfigEntry.Gui.Tooltip
        public boolean enableWitherSkeletonWeaponEnhancement = false; // 凋零骷髅武器升级（石、铁、钻石、下界合金剑）
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public float drownedSpawnWithShell = 0.03f;                 // 溺尸生成时自带鹦鹉螺壳的概率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 5)
        public int drownedSpawnFactor = 1;                          // 溺尸生成倍率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public double drownedSpawnWithTool = 0.1;                   // 溺尸生成时带有工具的概率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
        public int ghastSpawnFactor = 1;                            // 恶魂生成倍率
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public float piglinSpawnWithArmor = 0.1f;                   // 猪灵生成时自带金质盔甲的概率
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
    public HostileMobConfig hostileMobConfig = new HostileMobConfig();
}
