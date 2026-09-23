package com.frankfurtlin.mixinenhance.mixin.enchantment;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
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
    @Inject(method = "areCompatible", at = @At("HEAD"), cancellable = true)
    private static void canBeCombined(Holder<Enchantment> first, Holder<Enchantment> second, CallbackInfoReturnable<Boolean> cir){
        // 是否允许锋利、亡灵杀手、节肢杀手、破甲、致密不冲突
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeDamageEnchantmentConflict){
            if (first.is(EnchantmentTags.DAMAGE_EXCLUSIVE) && second.is(EnchantmentTags.DAMAGE_EXCLUSIVE)){
                cir.setReturnValue(true);
            }
        }
        // 是否允许保护、爆炸保护、弹射物保护、火焰保护不冲突
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeProtectionEnchantmentConflict){
            if (first.is(EnchantmentTags.ARMOR_EXCLUSIVE) && second.is(EnchantmentTags.ARMOR_EXCLUSIVE)){
                cir.setReturnValue(true);
            }
        }
        // 是否允许弓经验修补、无限不冲突
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeBowEnchantmentConflict){
            if (first.is(EnchantmentTags.BOW_EXCLUSIVE) && second.is(EnchantmentTags.BOW_EXCLUSIVE)){
                cir.setReturnValue(true);
            }
        }
        // 是否允许弩多重射击、穿透不冲突
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeCrossbowEnchantmentConflict){
            if (first.is(EnchantmentTags.CROSSBOW_EXCLUSIVE) && second.is(EnchantmentTags.CROSSBOW_EXCLUSIVE)){
                cir.setReturnValue(true);
            }
        }
        // 是否允许靴子深海探索者、冰霜行者不冲突
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeBootEnchantmentConflict){
            if (first.is(EnchantmentTags.BOOTS_EXCLUSIVE) && second.is(EnchantmentTags.BOOTS_EXCLUSIVE)){
                cir.setReturnValue(true);
            }
        }
    }
}
