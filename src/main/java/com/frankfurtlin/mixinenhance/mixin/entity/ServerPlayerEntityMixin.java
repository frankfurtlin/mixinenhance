package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/5/19 23:45
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {
    // 死亡不掉落（重生时复制背包与经验）
    // 1.21.11 中 GameRules.get(GameRule) 改为 GameRules.getValue(GameRule)
    @Redirect(method = "restoreFrom", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;"))
    private Object copyFrom(GameRules instance, GameRule<Boolean> rule) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.keepInventory){
            return Boolean.TRUE;
        }
        return instance.get(rule);
    }
}
