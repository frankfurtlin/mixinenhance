package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/18 21:42
 */
@Mixin(BaseSpawner.class)
public abstract class MobSpawnerLogicMixin {
    @Shadow
    protected abstract SpawnData getOrCreateNextSpawnData(@Nullable Level world, RandomSource random, BlockPos pos);


    // 刷怪笼改造成刷怪塔(刷怪笼上面方块需要是红石块)
    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void enableSpawnerFarm(ServerLevel world, BlockPos pos, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().defaultModuleConfig.enableSpawnerFarm) {
            // 刷怪笼上面方块需要是红石块
            if (!(world.getBlockState(pos.above()).getBlock() == Blocks.REDSTONE_BLOCK)) {
                return;
            }

            RandomSource random = world.getRandom();
            if (random.nextInt(20 * 8 / MixinEnhanceClient.getConfig().defaultModuleConfig.spawnerFarmRate) >= 1) {
                ci.cancel();
                return;
            }
            SpawnData mobSpawnerEntry = this.getOrCreateNextSpawnData(world, random, pos);

            CompoundTag nbtCompound = mobSpawnerEntry.getEntityToSpawn();
            // Temporarily simplified due to API changes
            ci.cancel();
        }
    }
}
