package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceConfig;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/16 13:58
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin {
    /**
     * 经验球合并的扫描半径（格）。
     *
     * <p>原版 {@code scanForMerges} 只扫「自己包围盒外扩 0.5 格」，一次掉一大片经验时会留下一堆小球，
     * 实体多、卡顿也重。这里沿用原版自己的合并流程（{@code canMerge} → {@code merge}），
     * 只把扫描范围放大到本值。</p>
     */
    private static final double MERGE_RADIUS = 8.0;

    /**
     * 原版 {@code canMerge} 要求两个球的实体 ID 差值是 40 的整数倍（一个粗略的分组，避免无谓匹配）。
     * 这个限制留着会让附近大多数球根本匹配不上，改为模 1（恒成立）即只受距离与数值限制。
     */
    private static final int MERGE_ID_MODULO = 1;

    // 玩家吸收经验无冷却
    @ModifyConstant(method = "playerTouch", constant = @Constant(intValue = 2), require = 0)
    private int enablePlayerExpPickUpNoDelay(int original) {
        if (MixinEnhanceConfig.get().playerConfig.enablePlayerExpPickUpNoDelay) {
            return 0;
        }
        return original;
    }

    // 经验球合并（1/2）：放大原版合并的扫描范围
    @ModifyConstant(method = "scanForMerges", constant = @Constant(doubleValue = 0.5D), require = 0)
    private double mixinEnhance$mergeRadius(double original) {
        return MixinEnhanceConfig.get().playerConfig.enableXpMerge ? MERGE_RADIUS : original;
    }

    // 经验球合并（2/2）：去掉原版「实体 ID 差必须是 40 的整数倍」的限制
    // 注意：目标 canMerge(ExperienceOrb,int,int) 是静态方法，回调也必须是 static
    @ModifyConstant(method = "canMerge(Lnet/minecraft/world/entity/ExperienceOrb;II)Z",
        constant = @Constant(intValue = 40), require = 0)
    private static int mixinEnhance$mergeIdModulo(int original) {
        return MixinEnhanceConfig.get().playerConfig.enableXpMerge ? MERGE_ID_MODULO : original;
    }
}
