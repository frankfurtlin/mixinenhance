package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
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
    // 死亡不掉落
    @Redirect(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean dropInventory(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.keepInventory){
            return true;
        }
        return instance.getBoolean(GameRules.KEEP_INVENTORY);
    }

    // 死亡不掉落
    @Redirect(method = "getExperienceToDrop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean getExperienceToDrop(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.keepInventory){
            return true;
        }
        return instance.getBoolean(GameRules.KEEP_INVENTORY);
    }
}
