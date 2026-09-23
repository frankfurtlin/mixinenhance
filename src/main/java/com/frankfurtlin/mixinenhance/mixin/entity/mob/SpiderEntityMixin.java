package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.monster.spider.Spider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/13 17:36
 */
@Mixin(Spider.class)
public abstract class SpiderEntityMixin {
    // 修改蜘蛛生成时带有药水效果的概率
    @ModifyConstant(method = "finalizeSpawn", constant = @Constant(floatValue = 0.1f))
    private float spiderSpawnWithEffect(float original) {
        return MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.spiderSpawnWithEffect;
    }
}
