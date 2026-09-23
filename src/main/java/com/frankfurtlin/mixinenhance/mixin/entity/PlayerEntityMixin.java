package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/5/19 23:34
 */
@Mixin(Player.class)
public abstract class PlayerEntityMixin {
    // 死亡不掉落（物品）
    // 26.3 中玩家物品掉落由 dropEquipment 处理，规则读取为 GameRules.get(GameRule)
    @Redirect(method = "dropEquipment", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;"))
    private Object dropInventory(GameRules instance, GameRule<Boolean> rule) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.keepInventory){
            return Boolean.TRUE;
        }
        return instance.get(rule);
    }

    // 死亡不掉落（经验）
    // 26.3 中 getExperienceToDrop 更名为 getBaseExperienceReward
    @Redirect(method = "getBaseExperienceReward", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;"))
    private Object getBaseExperienceReward(GameRules instance, GameRule<Boolean> rule) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.keepInventory){
            return Boolean.TRUE;
        }
        return instance.get(rule);
    }
}
