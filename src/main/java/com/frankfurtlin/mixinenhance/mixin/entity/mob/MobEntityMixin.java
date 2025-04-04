package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.EquipmentDropChances;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 15:32
 */
@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @Shadow
    private EquipmentDropChances equipmentDropChances;

    // 修改怪物死亡时工具及盔甲掉落的概率
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.equipmentDropChances = new EquipmentDropChances(Util.mapEnum(EquipmentSlot.class,
            slot -> MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.armorAndHandDropChance));
    }

    // 根据难度系数修改怪物死亡时掉落的经验
    @Inject(method = "getExperienceToDrop", at = @At(value = "RETURN"), cancellable = true)
    private void difficultyIndex2XpDrop(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex * cir.getReturnValue());
    }

    // 修改怪物生成时自带盔甲的概率
    @ModifyConstant(method = "initEquipment", constant = @Constant(floatValue = 0.15F))
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
    @ModifyConstant(method = "enchantMainHandItem", constant = @Constant(floatValue = 0.25F))
    private float enchantMainHandItemChance(float chance) {
        return MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.enchantmentMainHandChance;
    }

    // 修改怪物生成时盔甲附魔的概率
    @ModifyConstant(method = "enchantEquipment*", constant = @Constant(floatValue = 0.5F))
    private float enchantmentArmorChance(float chance) {
        return MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.enchantmentArmorChance;
    }

    // 根据难度系数修改单个区块怪物的数量
    @ModifyConstant(method = "getLimitPerChunk", constant = @Constant(intValue = 4))
    private int difficultyIndex2LimitPerChunk(int limit) {
        return MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.difficultyIndex * limit;
    }
}
