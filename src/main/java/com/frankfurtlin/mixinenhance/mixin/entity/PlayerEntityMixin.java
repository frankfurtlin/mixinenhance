package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/5/19 23:34
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    // 死亡不掉落（物品）
    // 1.21.11 中 GameRules.get(GameRule) 改为 GameRules.getValue(GameRule)
    @Redirect(method = "dropInventory", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/rule/GameRules;getValue(Lnet/minecraft/world/rule/GameRule;)Ljava/lang/Object;"))
    private Object dropInventory(GameRules instance, GameRule<Boolean> rule) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.keepInventory){
            return Boolean.TRUE;
        }
        return instance.getValue(rule);
    }

    // 死亡不掉落（经验）
    @Redirect(method = "getExperienceToDrop", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/rule/GameRules;getValue(Lnet/minecraft/world/rule/GameRule;)Ljava/lang/Object;"))
    private Object getExperienceToDrop(GameRules instance, GameRule<Boolean> rule) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.keepInventory){
            return Boolean.TRUE;
        }
        return instance.getValue(rule);
    }
}
