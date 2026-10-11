package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.DropChances;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 15:32
 */
@Mixin(Mob.class)
public abstract class MobEntityMixin {
    /** 盔甲材质档位数：皮、铜、金、锁链、铁、钻石、下界合金（与 26.3 原版一致） */
    private static final int ARMOR_LEVEL_COUNT = 7;

    @Shadow
    private DropChances dropChances;

    // 修改怪物死亡时工具及盔甲掉落的概率
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        Map<EquipmentSlot, Float> chances = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            chances.put(slot, MixinEnhanceClient.getConfig().entityConfig.mobConfig.armorAndHandDropChance);
        }
        this.dropChances = new DropChances(chances);
    }

    // 修改怪物生成时自带盔甲的概率
    @ModifyConstant(method = "populateDefaultEquipmentSlots", constant = @Constant(floatValue = 0.15F))
    private float spawnEquipmentChance(float chance) {
        return MixinEnhanceClient.getConfig().entityConfig.mobConfig.spawnEquipmentChance;
    }

    /**
     * @author frankslin
     * @reason 修改怪物生成时自带盔甲的等级（皮、铜、金、锁链、铁、钻石、下界合金，与 26.3 原版材质一致）
     */
    @Overwrite
    public static @Nullable Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int equipmentLevel) {
        int level = equipmentLevel;
        if (MixinEnhanceClient.getConfig().entityConfig.mobConfig.enableMobArmorEnhancement) {
            level = new Random().nextInt(ARMOR_LEVEL_COUNT);
        }
        return switch (equipmentSlot) {
            case HEAD -> switch (level) {
                case 0 -> Items.LEATHER_HELMET;
                case 1 -> Items.COPPER_HELMET;
                case 2 -> Items.GOLDEN_HELMET;
                case 3 -> Items.CHAINMAIL_HELMET;
                case 4 -> Items.IRON_HELMET;
                case 5 -> Items.DIAMOND_HELMET;
                default -> Items.NETHERITE_HELMET;
            };
            case CHEST -> switch (level) {
                case 0 -> Items.LEATHER_CHESTPLATE;
                case 1 -> Items.COPPER_CHESTPLATE;
                case 2 -> Items.GOLDEN_CHESTPLATE;
                case 3 -> Items.CHAINMAIL_CHESTPLATE;
                case 4 -> Items.IRON_CHESTPLATE;
                case 5 -> Items.DIAMOND_CHESTPLATE;
                default -> Items.NETHERITE_CHESTPLATE;
            };
            case LEGS -> switch (level) {
                case 0 -> Items.LEATHER_LEGGINGS;
                case 1 -> Items.COPPER_LEGGINGS;
                case 2 -> Items.GOLDEN_LEGGINGS;
                case 3 -> Items.CHAINMAIL_LEGGINGS;
                case 4 -> Items.IRON_LEGGINGS;
                case 5 -> Items.DIAMOND_LEGGINGS;
                default -> Items.NETHERITE_LEGGINGS;
            };
            case FEET -> switch (level) {
                case 0 -> Items.LEATHER_BOOTS;
                case 1 -> Items.COPPER_BOOTS;
                case 2 -> Items.GOLDEN_BOOTS;
                case 3 -> Items.CHAINMAIL_BOOTS;
                case 4 -> Items.IRON_BOOTS;
                case 5 -> Items.DIAMOND_BOOTS;
                default -> Items.NETHERITE_BOOTS;
            };
            default -> null;
        };
    }

    // 修改怪物生成时主手工具附魔的概率
    @ModifyConstant(method = "enchantSpawnedWeapon", constant = @Constant(floatValue = 0.25F))
    private float enchantMainHandItemChance(float chance) {
        return MixinEnhanceClient.getConfig().entityConfig.mobConfig.enchantmentMainHandChance;
    }

    // 修改怪物生成时盔甲附魔的概率
    // 26.3 中 0.5F 常量位于 enchantSpawnedArmor
    @ModifyConstant(method = "enchantSpawnedArmor", constant = @Constant(floatValue = 0.5F))
    private float enchantmentArmorChance(float chance) {
        return MixinEnhanceClient.getConfig().entityConfig.mobConfig.enchantmentArmorChance;
    }
}
