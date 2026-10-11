package com.frankfurtlin.mixinenhance.mixin.block;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 霜冰（冰霜行者踩出的冰）融化调整：在超热环境（地狱等，水会蒸发的地方）里改成熔岩。
 *
 * <p>26.3 的原版 {@code IceBlock#melt} 会先判断 {@code EnvironmentAttributes.WATER_EVAPORATES}：
 * 为真时直接移除方块（不生成水），否则放置 {@code meltsInto()}（水）。
 * 因此这里只在「超热环境 + 开启特性」时替换成熔岩，其余情况一律交还原版 melt，
 * 不再像以前那样整段复刻（旧写法把超热判定删掉了，导致地狱里也会生成水）。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/5/15 23:01
 */
@Mixin(FrostedIceBlock.class)
public abstract class FrostedIceBlockMixin extends IceBlock {
    public FrostedIceBlockMixin(Properties settings) {
        super(settings);
    }

    // 在地狱的时候霜冰化成熔岩
    @Redirect(method = "slightlyMelt", require = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FrostedIceBlock;melt(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void increaseAge(FrostedIceBlock instance, BlockState blockState, Level world, BlockPos blockPos) {
        meltIntoLavaOrVanilla(blockState, world, blockPos);
    }

    // 在地狱的时候霜冰化成熔岩
    @Redirect(method = "neighborChanged", require = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FrostedIceBlock;melt(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void neighborUpdate(FrostedIceBlock instance, BlockState blockState, Level world, BlockPos blockPos) {
        meltIntoLavaOrVanilla(blockState, world, blockPos);
    }

    /**
     * 超热环境下开启特性时放熔岩；其余情况全部交还原版 {@code melt}。
     */
    private void meltIntoLavaOrVanilla(BlockState blockState, Level world, BlockPos blockPos) {
        if (MixinEnhanceClient.getConfig().blockConfig.enableFrostWalkerWorkOnLava
            && world.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, blockPos)) {
            world.setBlockAndUpdate(blockPos, Blocks.LAVA.defaultBlockState());
            world.neighborChanged(blockPos, Blocks.LAVA, null);
            return;
        }
        // 未开启特性 / 非超热环境：完全按原版处理（原版在此会生成水，超热环境则移除方块）
        super.melt(blockState, world, blockPos);
    }
}
