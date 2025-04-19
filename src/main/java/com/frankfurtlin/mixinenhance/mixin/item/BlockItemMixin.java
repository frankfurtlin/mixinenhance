package com.frankfurtlin.mixinenhance.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    @Shadow public abstract @Nullable ItemPlacementContext getPlacementContext(ItemPlacementContext context);

    /**
     * @author frankslin
     * @reason 放置方块
     */
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at= @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V"))
    private void placeSpawner(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemPlacementContext itemPlacementContext = this.getPlacementContext(context);
        if (itemPlacementContext == null) {
            return;
        }
        BlockPos blockPos = itemPlacementContext.getBlockPos();
        ItemStack itemStack = itemPlacementContext.getStack();
        World world = itemPlacementContext.getWorld();
        // 刷怪笼放置的时候带上nbt标签
        if (itemStack.isOf(Items.SPAWNER)) {
            NbtComponent nbtComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA);
            if (nbtComponent != null) {
                NbtCompound nbtCompound = nbtComponent.copyNbt();
                if (nbtCompound.contains("spawnerData")) {
                    NbtCompound spawnerData = (NbtCompound)nbtCompound.get("spawnerData");
                    BlockEntity blockEntity = world.getBlockEntity(blockPos);
                    if (blockEntity instanceof MobSpawnerBlockEntity spawnerBlockEntity && spawnerData != null) {
                        spawnerBlockEntity.read(spawnerData, world.getRegistryManager());
                    }
                }
            }
        }
    }
}
