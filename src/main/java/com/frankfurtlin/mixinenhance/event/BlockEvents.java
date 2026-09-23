package com.frankfurtlin.mixinenhance.event;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/11 21:14
 */
public class BlockEvents {
    public static boolean onBlockBreak(Level world, Player player, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        if (world.isClientSide()) {
            return true;
        }

        if (player.isCreative()) {
            return true;
        }

        ItemStack handStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!hasSilkTouch(world, handStack)) {
            handStack = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!hasSilkTouch(world, handStack)) {
                return true;
            }
        }

        boolean isPickaxe = handStack.is(ItemTags.PICKAXES);

        Block block = blockState.getBlock();

        ItemStack outStack = null;
        if (((MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableSpawnerDropWithSilkTouch && block.equals(Blocks.SPAWNER)) ||
            (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableTrialSpawnerDropWithSilkTouch && block.equals(Blocks.TRIAL_SPAWNER))) &&
            isPickaxe) {
            if (blockEntity == null) {
                blockEntity = world.getBlockEntity(blockPos);
            }

            Entity spawnerDisplayEntity = null;
            CompoundTag spawnerData = null;

            if (blockEntity instanceof SpawnerBlockEntity mobSpawnerBlockEntity) {
                spawnerDisplayEntity = mobSpawnerBlockEntity.getSpawner().getOrCreateDisplayEntity(world, blockPos);
                spawnerData = mobSpawnerBlockEntity.saveWithFullMetadata(world.registryAccess());
            } else if (blockEntity instanceof TrialSpawnerBlockEntity trialSpawnerBlockEntity) {
                spawnerData = trialSpawnerBlockEntity.saveWithFullMetadata(world.registryAccess());
            }

            outStack = blockState.getCloneItemStack(world, blockPos, true);
            if (spawnerData != null) {
                CompoundTag nbtCompound = new CompoundTag();
                nbtCompound.put("spawnerData", spawnerData);
                CustomData.set(DataComponents.CUSTOM_DATA, outStack, nbtCompound);

                if (spawnerDisplayEntity != null) {
                    outStack.set(DataComponents.CUSTOM_NAME, spawnerDisplayEntity.getName().copy().append(Component.literal(" ").append(block.getName())));
                }
            }
        } else if ((MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableVaultDropWithSilkTouch && block.equals(Blocks.VAULT)) ||
            (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableBuddingAmethystDropWithSilkTouch && block.equals(Blocks.BUDDING_AMETHYST)) ||
            (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableFarmlandDropWithSilkTouch && block.equals(Blocks.FARMLAND)) ||
            (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableSuspiciousSandDropWithSilkTouch && block.equals(Blocks.SUSPICIOUS_SAND)) ||
            (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableSuspiciousGravelDropWithSilkTouch && block.equals(Blocks.SUSPICIOUS_GRAVEL))) {
            outStack = blockState.getCloneItemStack(world, blockPos, true);
        }

        if (outStack != null) {
            world.addFreshEntity(new ItemEntity(world, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, outStack));
            world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
            handStack.hurtWithoutBreaking(1, player);
            world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.gameEvent(player, GameEvent.BLOCK_DESTROY, blockPos);
            return false;
        }

        return true;
    }

    public static boolean hasSilkTouch(Level world, ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), itemStack) >= 1;
    }

}
