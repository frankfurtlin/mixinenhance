package com.frankfurtlin.mixinenhance.mixin.enchantment;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.ReplaceDisk;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/17 0:14
 */
@Mixin(ReplaceDisk.class)
public abstract class ReplaceDiskEnchantmentEffectMixin {
    @Shadow
    @Final
    private Vec3i offset;

    @Shadow
    @Final
    private LevelBasedValue radius;

    @Shadow
    @Final
    private LevelBasedValue height;

    @Shadow
    @Final
    private Holder<BlockStateProvider> blockState;

    @Shadow
    @Final
    private Optional<Holder<GameEvent>> triggerGameEvent;

    // 冰霜行者可以在熔岩上行走
    @Inject(method = "apply", at = @At("HEAD"))
    private void changeEnchantments(ServerLevel world, int level, EnchantedItemInUse context, Entity user, Vec3 pos, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().defaultModuleConfig.enableFrostWalkerWorkOnLava) {
            BlockPos blockPos = BlockPos.containing(pos).offset(this.offset);
            RandomSource random = user.getRandom();
            int i = (int) this.radius.calculate(level);
            int j = (int) this.height.calculate(level);

            Optional<BlockPredicate> predicateLava = Optional.of(
                BlockPredicate.allOf(
                    BlockPredicate.matchesTag(new Vec3i(0, 1, 0), BlockTags.AIR),
                    BlockPredicate.matchesBlocks(Blocks.LAVA),
                    BlockPredicate.matchesFluids(Fluids.LAVA),
                    BlockPredicate.unobstructed()
                )
            );

            for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset(-i, 0, -i), blockPos.offset(i, Math.min(j - 1, 0), i))) {
                if (blockPos2.distToCenterSqr(pos.x(), (double) blockPos2.getY() + 0.5, pos.z()) < (double) Mth.square(i)
                    && predicateLava.map(predicate -> predicate.test(world, blockPos2)).orElse(true)
                    && world.setBlockAndUpdate(blockPos2, this.blockState.value().getState(world, random, blockPos2))) {
                    this.triggerGameEvent.ifPresent(gameEvent -> world.gameEvent(user, gameEvent, blockPos2));
                }
            }
        }
    }
}
