package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.monster.Ghast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 19:13
 */
@Mixin(Ghast.class)
public abstract class GhastEntityMixin {
    // 修改恶魂生成率
    // 26.3 中 canSpawn 更名为 checkGhastSpawnRules
    @ModifyConstant(method = "checkGhastSpawnRules", constant = @Constant(intValue = 20))
    private static int spawnRate(int constant){
        return 20 / MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.ghastSpawnFactor;
    }
}
