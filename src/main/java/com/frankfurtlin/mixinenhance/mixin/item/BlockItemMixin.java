package com.frankfurtlin.mixinenhance.mixin.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 放置刷怪笼 / 试炼刷怪笼时还原方块实体 NBT（即笼子里装的是什么怪物）。
 *
 * <p>为什么必须自己写而不能交给原版：掉落物上的数据是精准采集时存进
 * {@code CUSTOM_DATA.spawnerData} 的自定义字段，而原版 {@code BlockItem#place}
 * 只认 {@code DataComponents.BLOCK_ENTITY_DATA}；更关键的是，26.3 里
 * {@code MOB_SPAWNER} / {@code TRIAL_SPAWNER} 都属于
 * {@code BlockEntityTypes.OP_ONLY_CUSTOM_DATA}，{@code updateCustomBlockEntityTag}
 * 遇到非管理员玩家会直接返回 false。所以在生存模式下只能在这里直接写入方块实体。</p>
 *
 * <p>注入点在 {@code setPlacedBy} 调用之前：此时方块与方块实体都已创建完毕
 * （原版 {@code updateCustomBlockEntityTag} 也已执行完），可以安全地覆盖数据。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/11 0:52
 */
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"))
    private void placeSpawner(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = context.getLevel();
        // 只在服务端写入，客户端由 sendBlockUpdated 同步；原版的组件写入同样是服务端行为
        if (level.isClientSide()) {
            return;
        }

        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        // 绝大多数方块放置会在这里被过滤掉，开销可忽略
        if (!(blockEntity instanceof SpawnerBlockEntity) && !(blockEntity instanceof TrialSpawnerBlockEntity)) {
            return;
        }

        CustomData customData = context.getItemInHand().get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }
        CompoundTag spawnerData = customData.copyTag().getCompoundOrEmpty("spawnerData");
        if (spawnerData.isEmpty()) {
            return;
        }

        // 目标方块实体类型已确定，标签里多余的 x/y/z/id 会被忽略
        blockEntity.loadWithComponents(
            TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), spawnerData));
        blockEntity.setChanged();
        level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_ALL);
    }
}
