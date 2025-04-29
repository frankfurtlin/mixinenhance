package com.frankfurtlin.mixinenhance.mixin.enchantment;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.entity.ReplaceDiskEnchantmentEffect;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/17 0:14
 */
@Mixin(ReplaceDiskEnchantmentEffect.class)
public abstract class ReplaceDiskEnchantmentEffectMixin {
    @Shadow
    @Final
    private Vec3i offset;

    @Shadow
    @Final
    private EnchantmentLevelBasedValue radius;

    @Shadow
    @Final
    private EnchantmentLevelBasedValue height;

    @Shadow
    @Final
    private BlockStateProvider blockState;

    @Shadow
    @Final
    private Optional<RegistryEntry<GameEvent>> triggerGameEvent;

    // 冰霜行者可以在熔岩上行走
    @Inject(method = "apply", at = @At("HEAD"))
    private void changeEnchantments(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().defaultModuleConfig.enableFrostWalkerWorkOnLava) {
            BlockPos blockPos = BlockPos.ofFloored(pos).add(this.offset);
            Random random = user.getRandom();
            int i = (int) this.radius.getValue(level);
            int j = (int) this.height.getValue(level);

            Optional<BlockPredicate> predicateLava = Optional.of(
                BlockPredicate.allOf(
                    BlockPredicate.matchingBlockTag(new Vec3i(0, 1, 0), BlockTags.AIR),
                    BlockPredicate.matchingBlocks(Blocks.LAVA),
                    BlockPredicate.matchingFluids(Fluids.LAVA),
                    BlockPredicate.unobstructed()
                )
            );

            for (BlockPos blockPos2 : BlockPos.iterate(blockPos.add(-i, 0, -i), blockPos.add(i, Math.min(j - 1, 0), i))) {
                if (blockPos2.getSquaredDistanceFromCenter(pos.getX(), (double) blockPos2.getY() + 0.5, pos.getZ()) < (double) MathHelper.square(i)
                    && predicateLava.map(predicate -> predicate.test(world, blockPos2)).orElse(true)
                    && world.setBlockState(blockPos2, this.blockState.get(random, blockPos2))) {
                    this.triggerGameEvent.ifPresent(gameEvent -> world.emitGameEvent(user, gameEvent, blockPos2));
                }
            }
        }
    }
}
