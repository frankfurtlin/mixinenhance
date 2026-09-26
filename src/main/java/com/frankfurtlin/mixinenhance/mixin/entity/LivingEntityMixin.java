package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 10:54
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // 不死图腾在背包中生效
    @Redirect(method = "checkTotemDeathProtection", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack inventoryTotemEnabled(LivingEntity livingEntity, InteractionHand hand) {
        if (MixinEnhanceClient.getConfig().itemModuleConfig.inventoryTotemEnabled && livingEntity instanceof Player) {
            for (int i = 0; i < ((Player) livingEntity).getInventory().getContainerSize(); i++) {
                ItemStack itemStack = ((Player) livingEntity).getInventory().getItem(i);
                if (itemStack.is(Items.TOTEM_OF_UNDYING)) {
                    return itemStack;
                }
            }
        }
        return livingEntity.getItemInHand(hand);
    }

    // 玩家在水中不减速
    // 1.21.11 将 travelInFluid 拆分为 travelInWater / travelInLava
    @ModifyConstant(method = "travelInWater", constant = @Constant(floatValue = 0.02f, ordinal = 0))
    private float enablePlayerNoSlowInWater(float constant){
        LivingEntity livingEntity = (LivingEntity) (Object)this;
        if(MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.enablePlayerNoSlowInWater && livingEntity instanceof Player){
            return 0.04f;
        }
        return constant;
    }

    // 玩家在熔岩中不减速
    // 1.21.11 将 travelInFluid 拆分为 travelInWater / travelInLava
    @ModifyConstant(method = "travelInLava", constant = @Constant(doubleValue = 0.5))
    private double enablePlayerNoSlowInLava(double constant){
        LivingEntity livingEntity = (LivingEntity) (Object)this;
        if(MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.enablePlayerNoSlowInLava && livingEntity instanceof Player){
            return 0.9;
        }
        return constant;
    }

    // 修改凋零骷髅掉落头颅的概率
    // 1.21.11 中 WitherSkeletonEntity 不再重写 dropEquipment，改在此处注入
    @Inject(method = "dropCustomDeathLoot", at = @At("TAIL"))
    private void dropHead(ServerLevel world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (livingEntity instanceof WitherSkeleton) {
            float dropRate = MixinEnhanceClient.getConfig().entityModuleConfig.hostileMobConfig.witherSkeletonSkullDropRate;
            if (livingEntity.getRandom().nextFloat() < dropRate) {
                livingEntity.spawnAtLocation(world, Items.WITHER_SKELETON_SKULL);
            }
        }
    }

    // 怪物死亡时掉落对应的刷怪笼
    @Inject(method = "createWitherRose", at = @At("TAIL"))
    private void injected(LivingEntity adversary, CallbackInfo ci) {
        Level world = ((LivingEntity) (Object) this).level();
        if (world.isClientSide()) {
            return;
        }
        if (adversary instanceof Player) {
            RandomSource random = world.getRandom();
            if (random.nextDouble() < MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.dieWithSpawner) {
                BlockPos blockPos = ((LivingEntity) (Object) this).blockPosition();
                if (world.getBlockState(blockPos).isAir()) {
                    world.setBlock(blockPos, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_ALL);
                }
                BlockEntity blockEntity = world.getBlockEntity(blockPos);
                if (blockEntity instanceof SpawnerBlockEntity mobSpawnerBlockEntity) {
                    mobSpawnerBlockEntity.setEntityId(((LivingEntity) (Object) this).getType(), random);
                }
            }
        }
    }
}


