package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.block.Blocks;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
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
@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {
    @Shadow
    protected abstract MobSpawnerEntry getSpawnEntry(@Nullable World world, Random random, BlockPos pos);


    // 刷怪笼改造成刷怪塔(刷怪笼上面方块需要是红石块)
    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void enableSpawnerFarm(ServerWorld world, BlockPos pos, CallbackInfo ci) {
        if (MixinEnhanceClient.getConfig().defaultModuleConfig.enableSpawnerFarm) {
            // 刷怪笼上面方块需要是红石块
            if (!(world.getBlockState(pos.up()).getBlock() == Blocks.REDSTONE_BLOCK)) {
                return;
            }

            Random random = world.getRandom();
            if (random.nextInt(20 * 8 / MixinEnhanceClient.getConfig().defaultModuleConfig.spawnerFarmRate) >= 1) {
                ci.cancel();
                return;
            }
            MobSpawnerEntry mobSpawnerEntry = this.getSpawnEntry(world, random, pos);

            NbtCompound nbtCompound = mobSpawnerEntry.getNbt();
            // Temporarily simplified due to API changes
            ci.cancel();
        }
    }
}
