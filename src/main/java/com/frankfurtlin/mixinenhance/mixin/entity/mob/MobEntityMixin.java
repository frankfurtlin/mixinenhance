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
    @Shadow
    private DropChances dropChances;

    // 修改怪物死亡时工具及盔甲掉落的概率
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        Map<EquipmentSlot, Float> chances = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            chances.put(slot, MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.armorAndHandDropChance);
        }
        this.dropChances = new DropChances(chances);
    }

    // 修改怪物生成时自带盔甲的概率
    @ModifyConstant(method = "populateDefaultEquipmentSlots", constant = @Constant(floatValue = 0.15F))
    private float spawnEquipmentChance(float chance) {
        return MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.spawnEquipmentChance;
    }

    /**
     * @author frankslin
     * @reason 修改怪物生成时自带盔甲的等级（皮、金、锁链、铁、钻石、下届合金）
     */
    @Overwrite
    public static @Nullable Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int equipmentLevel) {
        int level = equipmentLevel;
        if (MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.enableMobArmorEnhancement) {
            Random random = new Random();
            level = random.nextInt(6);
        }
        switch (equipmentSlot) {
            case HEAD:
                if (level == 0) {
                    return Items.LEATHER_HELMET;
                } else if (level == 1) {
                    return Items.GOLDEN_HELMET;
                } else if (level == 2) {
                    return Items.CHAINMAIL_HELMET;
                } else if (level == 3) {
                    return Items.IRON_HELMET;
                } else if (level == 4) {
                    return Items.DIAMOND_HELMET;
                } else {
                    return Items.NETHERITE_HELMET;
                }
            case CHEST:
                if (level == 0) {
                    return Items.LEATHER_CHESTPLATE;
                } else if (level == 1) {
                    return Items.GOLDEN_CHESTPLATE;
                } else if (level == 2) {
                    return Items.CHAINMAIL_CHESTPLATE;
                } else if (level == 3) {
                    return Items.IRON_CHESTPLATE;
                } else if (level == 4) {
                    return Items.DIAMOND_CHESTPLATE;
                } else {
                    return Items.NETHERITE_CHESTPLATE;
                }
            case LEGS:
                if (level == 0) {
                    return Items.LEATHER_LEGGINGS;
                } else if (level == 1) {
                    return Items.GOLDEN_LEGGINGS;
                } else if (level == 2) {
                    return Items.CHAINMAIL_LEGGINGS;
                } else if (level == 3) {
                    return Items.IRON_LEGGINGS;
                } else if (level == 4) {
                    return Items.DIAMOND_LEGGINGS;
                } else {
                    return Items.NETHERITE_LEGGINGS;
                }
            case FEET:
                if (level == 0) {
                    return Items.LEATHER_BOOTS;
                } else if (level == 1) {
                    return Items.GOLDEN_BOOTS;
                } else if (level == 2) {
                    return Items.CHAINMAIL_BOOTS;
                } else if (level == 3) {
                    return Items.IRON_BOOTS;
                } else if (level == 4) {
                    return Items.DIAMOND_BOOTS;
                } else {
                    return Items.NETHERITE_BOOTS;
                }
            default:
                return null;
        }
    }

    // 修改怪物生成时主手工具附魔的概率
    @ModifyConstant(method = "enchantSpawnedWeapon", constant = @Constant(floatValue = 0.25F))
    private float enchantMainHandItemChance(float chance) {
        return MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.enchantmentMainHandChance;
    }

    // 修改怪物生成时盔甲附魔的概率
    // 26.3 中 0.5F 常量位于 enchantSpawnedArmor
    @ModifyConstant(method = "enchantSpawnedArmor", constant = @Constant(floatValue = 0.5F))
    private float enchantmentArmorChance(float chance) {
        return MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.enchantmentArmorChance;
    }
}
