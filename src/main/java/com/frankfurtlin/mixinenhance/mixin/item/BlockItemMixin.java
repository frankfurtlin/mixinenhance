package com.frankfurtlin.mixinenhance.mixin.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/11 0:52
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Shadow public abstract Block getBlock();

    @Shadow public abstract @Nullable ItemPlacementContext getPlacementContext(ItemPlacementContext context);

    @Shadow protected abstract @Nullable BlockState getPlacementState(ItemPlacementContext context);

    @Shadow protected abstract boolean place(ItemPlacementContext context, BlockState state);

    @Shadow protected abstract BlockState placeFromNbt(BlockPos pos, World world, ItemStack stack, BlockState state);

    @Shadow protected abstract boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state);

    @Unique
    private static void copyComponentsToBlockEntity(World world, BlockPos pos, ItemStack stack) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.readComponents(stack);
            blockEntity.markDirty();
        }
    }

    @Shadow protected abstract SoundEvent getPlaceSound(BlockState state);

    /**
     * @author frankslin
     * @reason 放置方块
     */
    @Overwrite
    public ActionResult place(ItemPlacementContext context) {
        if (!this.getBlock().isEnabled(context.getWorld().getEnabledFeatures())) {
            return ActionResult.FAIL;
        } else if (!context.canPlace()) {
            return ActionResult.FAIL;
        } else {
            ItemPlacementContext itemPlacementContext = this.getPlacementContext(context);
            if (itemPlacementContext == null) {
                return ActionResult.FAIL;
            } else {
                BlockState blockState = this.getPlacementState(itemPlacementContext);
                if (blockState == null) {
                    return ActionResult.FAIL;
                } else if (!this.place(itemPlacementContext, blockState)) {
                    return ActionResult.FAIL;
                } else {
                    BlockPos blockPos = itemPlacementContext.getBlockPos();
                    World world = itemPlacementContext.getWorld();
                    PlayerEntity playerEntity = itemPlacementContext.getPlayer();
                    ItemStack itemStack = itemPlacementContext.getStack();
                    BlockState blockState2 = world.getBlockState(blockPos);
                    if (blockState2.isOf(blockState.getBlock())) {
                        blockState2 = this.placeFromNbt(blockPos, world, itemStack, blockState2);
                        this.postPlacement(blockPos, world, playerEntity, itemStack, blockState2);
                        copyComponentsToBlockEntity(world, blockPos, itemStack);
                        blockState2.getBlock().onPlaced(world, blockPos, blockState2, playerEntity, itemStack);
                        if (playerEntity instanceof ServerPlayerEntity) {
                            Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)playerEntity, blockPos, itemStack);
                        }
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

                    BlockSoundGroup blockSoundGroup = blockState2.getSoundGroup();
                    world.playSound(
                        playerEntity,
                        blockPos,
                        this.getPlaceSound(blockState2),
                        SoundCategory.BLOCKS,
                        (blockSoundGroup.getVolume() + 1.0F) / 2.0F,
                        blockSoundGroup.getPitch() * 0.8F
                    );
                    world.emitGameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Emitter.of(playerEntity, blockState2));
                    itemStack.decrementUnlessCreative(1, playerEntity);
                    return ActionResult.SUCCESS;
                }
            }
        }
	}
}
