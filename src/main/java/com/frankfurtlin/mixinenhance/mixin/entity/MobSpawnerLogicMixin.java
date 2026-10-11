package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 刷怪笼秒变刷怪塔：刷怪笼六面中任意一个相邻位置放了红石块即视为激活，
 * 不再真正刷怪，而是直接把「该怪物类型的战利品」从红石块的反方向喷出。
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/18 21:42
 */
@Mixin(BaseSpawner.class)
public abstract class MobSpawnerLogicMixin {
    @Shadow
    protected abstract SpawnData getOrCreateNextSpawnData(@Nullable Level world, RandomSource random, BlockPos pos);


    // 刷怪笼改造成刷怪塔(前后左右上下任一相邻位置放置红石块)
    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void enableSpawnerFarm(ServerLevel world, BlockPos pos, CallbackInfo ci) {
        if (!MixinEnhanceClient.getConfig().blockConfig.spawnerFarmConfig.enableSpawnerFarm) {
            return;
        }

        // 收集所有相邻的红石块方向；一个都没有时保持原版行为
        List<Direction> redstoneSides = new ArrayList<>(6);
        for (Direction direction : Direction.values()) {
            if (world.getBlockState(pos.relative(direction)).is(Blocks.REDSTONE_BLOCK)) {
                redstoneSides.add(direction);
            }
        }
        if (redstoneSides.isEmpty()) {
            return;
        }

        // 已进入刷怪塔模式：本 tick 由本方法接管，原版刷怪逻辑不再执行
        ci.cancel();

        RandomSource random = world.getRandom();
        int rate = Math.max(1, MixinEnhanceClient.getConfig().blockConfig.spawnerFarmConfig.spawnerFarmRate);
        // 倍率越高越频繁：rate=1 约 8 秒一次，rate=8 约每秒一次
        if (random.nextInt(20 * 8 / rate) >= 1) {
            return;
        }

        // 取出该刷怪笼要刷的怪物
        SpawnData spawnData = this.getOrCreateNextSpawnData(world, random, pos);
        CompoundTag entityTag = spawnData.getEntityToSpawn();
        Optional<EntityType<?>> entityType = EntityType.by(
            TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), entityTag));
        if (entityType.isEmpty()) {
            return;
        }

        Optional<ResourceKey<LootTable>> lootTableKey = entityType.get().getDefaultLootTable();
        if (lootTableKey.isEmpty()) {
            return;
        }
        LootTable lootTable = world.getServer().reloadableRegistries().getLootTable(lootTableKey.get());
        LootParams lootParams = new LootParams.Builder(world).create(LootContextParamSets.EMPTY);

        // 相邻有多个红石块时随机取一个，产物从其「反方向」的面中心射出
        Direction outDirection = redstoneSides.get(random.nextInt(redstoneSides.size())).getOpposite();
        double speed = 0.3;
        double spawnX = pos.getX() + 0.5 + outDirection.getStepX() * 0.5;
        double spawnY = pos.getY() + 0.5 + outDirection.getStepY() * 0.5;
        double spawnZ = pos.getZ() + 0.5 + outDirection.getStepZ() * 0.5;
        double velocityX = outDirection.getStepX() * speed;
        double velocityY = outDirection.getStepY() * speed;
        double velocityZ = outDirection.getStepZ() * speed;

        for (ItemStack stack : lootTable.getRandomItems(lootParams)) {
            if (stack.isEmpty()) {
                continue;
            }
            world.addFreshEntity(new ItemEntity(world, spawnX, spawnY, spawnZ, stack,
                velocityX, velocityY, velocityZ));
        }
    }
}
