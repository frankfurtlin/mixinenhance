package com.frankfurtlin.mixinenhance.mixin.enchantment;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/29 9:04
 */
@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {
    @Inject(method = "canBeCombined", at = @At("HEAD"), cancellable = true)
    private static void canBeCombined(RegistryEntry<Enchantment> first, RegistryEntry<Enchantment> second, CallbackInfoReturnable<Boolean> cir){
        // 是否允许锋利、亡灵杀手、节肢杀手、破甲、致密不冲突
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeDamageEnchantmentConflict){
            if (first.isIn(EnchantmentTags.DAMAGE_EXCLUSIVE_SET) && second.isIn(EnchantmentTags.DAMAGE_EXCLUSIVE_SET)){
                cir.setReturnValue(true);
            }
        }
        // 是否允许保护、爆炸保护、弹射物保护、火焰保护不冲突
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeProtectionEnchantmentConflict){
            if (first.isIn(EnchantmentTags.ARMOR_EXCLUSIVE_SET) && second.isIn(EnchantmentTags.ARMOR_EXCLUSIVE_SET)){
                cir.setReturnValue(true);
            }
        }
        // 是否允许弓经验修补、无限不冲突
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeBowEnchantmentConflict){
            if (first.isIn(EnchantmentTags.BOW_EXCLUSIVE_SET) && second.isIn(EnchantmentTags.BOW_EXCLUSIVE_SET)){
                cir.setReturnValue(true);
            }
        }
        // 是否允许弩多重射击、穿透不冲突
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeCrossbowEnchantmentConflict){
            if (first.isIn(EnchantmentTags.CROSSBOW_EXCLUSIVE_SET) && second.isIn(EnchantmentTags.CROSSBOW_EXCLUSIVE_SET)){
                cir.setReturnValue(true);
            }
        }
        // 是否允许靴子深海探索者、冰霜行者不冲突
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeBootEnchantmentConflict){
            if (first.isIn(EnchantmentTags.BOOTS_EXCLUSIVE_SET) && second.isIn(EnchantmentTags.BOOTS_EXCLUSIVE_SET)){
                cir.setReturnValue(true);
            }
        }
    }
}
