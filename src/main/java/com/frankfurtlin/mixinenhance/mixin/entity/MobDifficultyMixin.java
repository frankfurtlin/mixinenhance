package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/9/23
 */
@Mixin(Mob.class)
public abstract class MobDifficultyMixin {
    // 统一应用难度倍率：只作用于敌对生物（Enemy），血量与攻击分别可配。
    // 相比原先每个怪物各写一个 mixin，这里按「基础属性值 × 倍率」缩放，
    // 新增的敌对生物只要实现 Enemy 即自动生效，无需再单独添加 mixin。
    @Inject(method = "<init>", at = @At("TAIL"))
    private void mixinEnhance$applyDifficulty(EntityType<? extends Mob> entityType, Level level, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (!(self instanceof Enemy)) {
            return;
        }
        double healthMultiplier = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.healthMultiplier;
        double attackMultiplier = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.attackMultiplier;

        if (healthMultiplier != 1.0) {
            AttributeInstance health = self.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.setBaseValue(health.getBaseValue() * healthMultiplier);
                self.setHealth(self.getMaxHealth());
            }
        }
        if (attackMultiplier != 1.0) {
            AttributeInstance attack = self.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attack != null) {
                attack.setBaseValue(attack.getBaseValue() * attackMultiplier);
            }
        }
    }
}
