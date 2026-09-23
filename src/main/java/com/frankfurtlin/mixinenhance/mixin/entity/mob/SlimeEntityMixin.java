package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/21 17:53
 */
@Mixin(Slime.class)
public abstract class SlimeEntityMixin extends Mob {
    protected SlimeEntityMixin(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    // 史莱姆的血量/攻击随尺寸动态变化（setSize 时重算），统一 mixin 无法覆盖，需在此按倍率缩放
    @Inject(method = "setSize", at = @At("TAIL"))
    private void customHealthAndAttackDamage(int size, boolean heal, CallbackInfo ci){
        int i = Mth.clamp(size, 1, 127);
        double healthMultiplier = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.healthMultiplier;
        double attackMultiplier = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.attackMultiplier;
        double health = i * i * healthMultiplier;
        double attack = i * attackMultiplier;
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(health);
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(attack);
    }
}
