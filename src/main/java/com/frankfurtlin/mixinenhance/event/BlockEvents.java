package com.frankfurtlin.mixinenhance.event;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/11 21:14
 */
public class BlockEvents {
    public static boolean onBlockBreak(World world, PlayerEntity player, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        if (world.isClient()) {
            return true;
        }

        if (player.isCreative()) {
            return true;
        }

        ItemStack handStack = player.getStackInHand(Hand.MAIN_HAND);
        if (!hasSilkTouch(world, handStack)) {
            handStack = player.getStackInHand(Hand.OFF_HAND);
            if (!hasSilkTouch(world, handStack)) {
                return true;
            }
        }

        boolean isPickaxe = handStack.isIn(ItemTags.PICKAXES);

        Block block = blockState.getBlock();

        ItemStack outStack = null;
        if (((MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableSpawnerDropWithSilkTouch && block.equals(Blocks.SPAWNER)) ||
            (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableTrialSpawnerDropWithSilkTouch && block.equals(Blocks.TRIAL_SPAWNER))) &&
            isPickaxe) {
            if (blockEntity == null) {
                blockEntity = world.getBlockEntity(blockPos);
            }

            Entity spawnerDisplayEntity = null;
            NbtCompound spawnerData = null;

            if (blockEntity instanceof MobSpawnerBlockEntity mobSpawnerBlockEntity) {
                spawnerDisplayEntity = mobSpawnerBlockEntity.getLogic().getRenderedEntity(world, blockPos);
                spawnerData = mobSpawnerBlockEntity.createNbtWithIdentifyingData(world.getRegistryManager());
            } else if (blockEntity instanceof TrialSpawnerBlockEntity trialSpawnerBlockEntity) {
                spawnerData = trialSpawnerBlockEntity.createNbtWithIdentifyingData(world.getRegistryManager());
            }

            outStack = blockState.getPickStack(world, blockPos, true);
            if (spawnerData != null) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.put("spawnerData", spawnerData);
                NbtComponent.set(DataComponentTypes.CUSTOM_DATA, outStack, nbtCompound);

                if (spawnerDisplayEntity != null) {
                    outStack.set(DataComponentTypes.CUSTOM_NAME, spawnerDisplayEntity.getName().copy().append(Text.literal(" ").append(block.getName())));
                }
            }
        } else if ((MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableVaultDropWithSilkTouch && block.equals(Blocks.VAULT)) ||
            (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableBuddingAmethystDropWithSilkTouch && block.equals(Blocks.BUDDING_AMETHYST)) ||
            (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableFarmlandDropWithSilkTouch && block.equals(Blocks.FARMLAND)) ||
            (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableSuspiciousSandDropWithSilkTouch && block.equals(Blocks.SUSPICIOUS_SAND)) ||
            (MixinEnhanceClient.getConfig().blockModuleConfig.silkTouchConfig.enableSuspiciousGravelDropWithSilkTouch && block.equals(Blocks.SUSPICIOUS_GRAVEL))) {
            outStack = blockState.getPickStack(world, blockPos, true);
        }

        if (outStack != null) {
            world.spawnEntity(new ItemEntity(world, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, outStack));
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
            handStack.damage(1, player);
            world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.emitGameEvent(player, GameEvent.BLOCK_DESTROY, blockPos);
            return false;
        }

        return true;
    }

    public static boolean hasSilkTouch(World world, ItemStack itemStack) {
        return EnchantmentHelper.getLevel(world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), itemStack) >= 1;
    }

}
