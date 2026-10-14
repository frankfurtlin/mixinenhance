package com.frankfurtlin.mixinenhance.mixin.block;

import com.frankfurtlin.mixinenhance.MixinEnhanceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 树叶快速凋零。
 *
 * <p>原版逻辑：树叶与原木断开连接后，其 distance 属性会逐步增长到 7（即 {@code DISTANCE == 7} 且
 * {@code !PERSISTENT}，等价于 LeavesBlock#decaying），但真正掉落要等一个「随机刻」才发生，
 * 整片树冠因此要等很久才消失。本 mixin 在距离重算（tick）后立即执行掉落，跳过随机刻等待，
 * 从而整体瞬间凋零。</p>
 *
 * <p>判定完全复用原版：仅当树叶已孤儿化（DISTANCE == 7）且非 persistent 才掉落。
 * 玩家手动放置的树叶带有 persistent 标记，永远不会进入该分支，因此不受影响。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onLeafTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!MixinEnhanceConfig.get().blockConfig.enableFastLeafDecay) {
            return;
        }
        BlockState current = level.getBlockState(pos);
        if (current.getBlock() instanceof LeavesBlock
                && !current.getValue(LeavesBlock.PERSISTENT)
                && current.getValue(LeavesBlock.DISTANCE) == 7) {
            Block.dropResources(current, level, pos);
            level.removeBlock(pos, false);
        }
    }
}
