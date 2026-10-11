package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/16 13:58
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin {
    /**
     * 经验球合并半径（格）。
     *
     * <p>原版 {@code scanForMerges} 只扫「自己包围盒外扩 0.5 格」，而且 {@code canMerge} 还额外要求
     * 两个球的实体 ID 差值是 40 的整数倍、数值完全相同 —— 所以一次掉一大片经验时会留下一堆小球，
     * 实体多、卡顿也重。这里在原版同一个注入点（{@code scanForMerges} 末尾，原版每 20 tick 才跑一次）
     * 额外扫一个大范围，把附近的同类经验球并成一个。</p>
     *
     * <p><b>只合并数值相同的经验球</b>：原版 {@code merge} 只累加 {@code count}、不累加数值
     * （数值相同是原版 {@code canMerge} 的前提），并不同值的球会直接丢经验。</p>
     */
    private static final double MERGE_RADIUS = 8.0;

    // 玩家吸收经验无冷却
    @ModifyConstant(method = "playerTouch", constant = @Constant(intValue = 2), require = 0)
    private int enablePlayerExpPickUpNoDelay(int original) {
        if (MixinEnhanceClient.getConfig().playerConfig.enablePlayerExpPickUpNoDelay) {
            return 0;
        }
        return original;
    }

    // 经验球合并：把附近同值的经验球并到一起（实体更少、拾取更快）
    @Inject(method = "scanForMerges", at = @At("TAIL"), require = 0)
    private void mixinEnhance$mergeNearbyOrbs(CallbackInfo ci) {
        if (!MixinEnhanceClient.getConfig().playerConfig.enableXpMerge) {
            return;
        }
        ExperienceOrb self = (ExperienceOrb) (Object) this;
        if (self.isRemoved() || !(self.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        List<ExperienceOrb> nearby = serverLevel.getEntities(
            EntityTypeTest.forClass(ExperienceOrb.class),
            self.getBoundingBox().inflate(MERGE_RADIUS),
            orb -> orb != self && !orb.isRemoved() && orb.getValue() == self.getValue());
        for (ExperienceOrb orb : nearby) {
            if (self.isRemoved()) {
                break;
            }
            mixinEnhance$merge(orb);
        }
    }

    /** 调用原版私有的 {@code merge(ExperienceOrb)} */
    @Invoker("merge")
    private void mixinEnhance$merge(ExperienceOrb other) {
        throw new AssertionError();
    }
}
