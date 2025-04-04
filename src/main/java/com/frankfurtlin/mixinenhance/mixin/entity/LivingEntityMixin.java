package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
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
    @Redirect(method = "tryUseDeathProtector", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/entity/LivingEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack inventoryTotemEnabled(LivingEntity livingEntity, Hand hand) {
        if (MixinEnhanceClient.getConfig().itemModuleConfig.inventoryTotemEnabled && livingEntity instanceof PlayerEntity) {
            for (int i = 0; i < ((PlayerEntity) livingEntity).getInventory().size(); i++) {
                ItemStack itemStack = ((PlayerEntity) livingEntity).getInventory().getStack(i);
                if (itemStack.isOf(Items.TOTEM_OF_UNDYING)) {
                    return itemStack;
                }
            }
        }
        return livingEntity.getStackInHand(hand);
    }

    // 玩家在水中不减速
    @ModifyConstant(method = "travelInFluid", constant = @Constant(floatValue = 0.02f, ordinal = 0))
    private float enablePlayerNoSlowInWater(float constant){
        LivingEntity livingEntity = (LivingEntity) (Object)this;
        if(MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.enablePlayerNoSlowInWater && livingEntity instanceof PlayerEntity){
            return 0.04f;
        }
        return constant;
    }

    // 玩家在熔岩中不减速
    @ModifyConstant(method = "travelInFluid", constant = @Constant(doubleValue = 0.5))
    private double enablePlayerNoSlowInLava(double constant){
        LivingEntity livingEntity = (LivingEntity) (Object)this;
        if(MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.enablePlayerNoSlowInLava && livingEntity instanceof PlayerEntity){
            return 0.9;
        }
        return constant;
    }

    // 怪物死亡时掉落对应的刷怪笼
    @Inject(method = "onKilledBy", at = @At("TAIL"))
    private void injected(LivingEntity adversary, CallbackInfo ci) {
        World world = ((LivingEntity) (Object) this).getWorld();
        if (world.isClient()) {
            return;
        }
        if (adversary instanceof PlayerEntity) {
            Random random = world.getRandom();
            if (random.nextDouble() < MixinEnhanceClient.getConfig().entityModuleConfig.mobConfig.dieWithSpawner) {
                BlockPos blockPos = ((LivingEntity) (Object) this).getBlockPos();
                if (world.getBlockState(blockPos).isAir()) {
                    world.setBlockState(blockPos, Blocks.SPAWNER.getDefaultState(), Block.NOTIFY_ALL);
                }
                BlockEntity blockEntity = world.getBlockEntity(blockPos);
                if (blockEntity instanceof MobSpawnerBlockEntity mobSpawnerBlockEntity) {
                    mobSpawnerBlockEntity.setEntityType(((LivingEntity) (Object) this).getType(), random);
                }
            }
        }
    }
}


