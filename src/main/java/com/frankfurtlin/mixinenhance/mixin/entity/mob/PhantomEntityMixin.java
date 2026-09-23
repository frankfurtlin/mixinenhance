package com.frankfurtlin.mixinenhance.mixin.entity.mob;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/21 15:27
 */
@Mixin(Phantom.class)
public abstract class PhantomEntityMixin extends Mob {
    @Shadow public abstract int getPhantomSize();

    protected PhantomEntityMixin(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    // 幻翼的攻击力随体型动态变化，统一 mixin 无法覆盖，需在此按攻击倍率缩放
    @Inject(method = "updatePhantomSizeInfo", cancellable = true, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/monster/Phantom;getAttribute(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;"))
    private void customAttackDamage(CallbackInfo ci){
        double attackMultiplier = MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.attackMultiplier;
        double attack = (6 + this.getPhantomSize()) * attackMultiplier;
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(attack);
        ci.cancel();
    }
}
