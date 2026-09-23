package com.frankfurtlin.mixinenhance.mixin.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/11 0:52
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Shadow public abstract Block getBlock();

    @Shadow public abstract @Nullable BlockPlaceContext updatePlacementContext(BlockPlaceContext context);

    /**
     * @author frankslin
     * @reason 放置方块
     */
    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"))
    private void placeSpawner(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        // Temporarily disabled due to API changes
        // ItemPlacementContext itemPlacementContext = this.getPlacementContext(context);
        // if (itemPlacementContext == null) {
        //     return;
        // }
        // BlockPos blockPos = itemPlacementContext.getBlockPos();
        // ItemStack itemStack = itemPlacementContext.getStack();
        // World world = itemPlacementContext.getWorld();
        // // 刷怪笼放置的时候带上nbt标签
        // if (itemStack.isOf(Items.SPAWNER) || itemStack.isOf(Items.TRIAL_SPAWNER)) {
        //     NbtComponent nbtComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        //     if (nbtComponent != null) {
        //         NbtCompound nbtCompound = nbtComponent.copyNbt();
        //         if (nbtCompound.contains("spawnerData")) {
        //             NbtCompound spawnerData = (NbtCompound)nbtCompound.get("spawnerData");
        //             BlockEntity blockEntity = world.getBlockEntity(blockPos);
        //             // Try to use the read method directly
        //             if (blockEntity instanceof MobSpawnerBlockEntity spawnerBlockEntity && spawnerData != null) {
        //                 spawnerBlockEntity.read(spawnerData, world.getRegistryManager());
        //             }
        //             if (blockEntity instanceof TrialSpawnerBlockEntity spawnerBlockEntity && spawnerData != null) {
        //                 spawnerBlockEntity.read(spawnerData, world.getRegistryManager());
        //             }
        //         }
        //     }
        // }
    }
}
