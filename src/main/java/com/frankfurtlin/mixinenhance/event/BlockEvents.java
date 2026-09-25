package com.frankfurtlin.mixinenhance.event;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/11 21:14
 */
public class BlockEvents {
    // 单次连锁破坏的数量上限，避免超大树 / 超大矿脉导致卡顿
    private static final int MAX_FALLING_TREE_BLOCKS = 128;
    private static final int MAX_VEIN_MINING_BLOCKS = 64;

    public static boolean onBlockBreak(Level world, Player player, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        if (world.isClientSide()) {
            return true;
        }

        if (player.isCreative()) {
            return true;
        }

        // 连锁破坏（砍树 / 连锁挖矿），不需要精准采集即可生效
        if (handleChainBreak(world, player, blockPos, blockState)) {
            return false;
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
            world.playSound(player, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                blockState.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            world.gameEvent(player, GameEvent.BLOCK_DESTROY, blockPos);
            return false;
        }

        return true;
    }

    /**
     * 连锁破坏：砍倒整棵树（原木）或连锁挖掉整条矿脉（矿石）。
     * 两者本质相同——从起始点 BFS 收集相连的同种方块后统一破坏，仅判定规则与数量上限不同。
     *
     * @return 是否已接管本次破坏（true 表示取消原版破坏流程）
     */
    private static boolean handleChainBreak(Level world, Player player, BlockPos origin, BlockState originState) {
        boolean fallingTree = MixinEnhanceClient.getConfig().blockModuleConfig.enableFallingTree;
        boolean veinMining = MixinEnhanceClient.getConfig().blockModuleConfig.enableVeinMining;
        if (!fallingTree && !veinMining) {
            return false;
        }

        if (fallingTree && originState.is(BlockTags.LOGS)) {
            breakConnectedBlocks(world, player, origin, MAX_FALLING_TREE_BLOCKS);
            return true;
        }
        if (veinMining && originState.is(BlockTags.ORES)) {
            breakConnectedBlocks(world, player, origin, MAX_VEIN_MINING_BLOCKS);
            return true;
        }
        return false;
    }

    /**
     * 从起始点 BFS 收集「相连且同种」的方块并全部破坏（掉落物品、消耗耐久）。
     */
    private static void breakConnectedBlocks(Level world, Player player, BlockPos origin, int maxBlocks) {
        BlockState originState = world.getBlockState(origin);
        Block targetBlock = originState.getBlock();
        // 提前取好破坏音：下方会把方块清空，之后再取就变成空气的音效了
        SoundEvent breakSound = originState.getSoundType().getBreakSound();

        Set<BlockPos> collected = new LinkedHashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        collected.add(origin);
        queue.add(origin);

        while (!queue.isEmpty() && collected.size() < maxBlocks) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (collected.contains(next)) {
                    continue;
                }
                if (world.getBlockState(next).getBlock() == targetBlock) {
                    collected.add(next);
                    queue.add(next);
                    if (collected.size() >= maxBlocks) {
                        break;
                    }
                }
            }
        }

        ItemStack tool = player.getItemInHand(InteractionHand.MAIN_HAND);
        for (BlockPos pos : collected) {
            BlockState state = world.getBlockState(pos);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            Block.dropResources(state, world, pos, blockEntity, player, tool);
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            world.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
            if (!tool.isEmpty()) {
                tool.hurtWithoutBreaking(1, player);
            }
        }

        // 整批只播一次（砍树木头声、挖矿石头声），避免连锁上百个方块时响成一片
        world.playSound(null, origin, breakSound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public static boolean hasSilkTouch(Level world, ItemStack itemStack) {
        return EnchantmentHelper.getItemEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), itemStack) >= 1;
    }

}
