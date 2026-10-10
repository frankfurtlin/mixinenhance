package com.frankfurtlin.mixinenhance.mixin.server;

import com.frankfurtlin.mixinenhance.util.FreeCamTracker;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 自由视角时把「区块加载中心」从玩家实体改为相机位置。
 *
 * 原版区块加载以玩家实体为中心，而自由视角下身体留在原地、只有相机飞走，
 * 导致相机飞到的区域并未加载（看到空洞并伴随卡顿）。
 * 这里替换 ChunkMap 中用于计算玩家追踪视图的位置，玩家实体本身不移动。
 *
 * 重要：move 与 updatePlayerStatus（removeEntity 也会调用）必须用同一个坐标，
 * 否则自由视角下玩家会在「相机区块」被加入、又在「真实区块」被移除，
 * 造成 chunkPlayers 记账错位（区块加载错乱、下线/死亡移除错位等）。
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Redirect(method = "move", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/entity/EntityAccess;)Lnet/minecraft/core/SectionPos;"))
    private static SectionPos mixinEnhance$useFreeCamCenterInMove(EntityAccess entity) {
        return useFreeCamCenter(entity);
    }

    // updatePlayerStatus 同样负责增删玩家追踪（move 与 removeEntity 都会走到），
    // 必须与 move 用同一坐标。require = 0：若 26.3 该方法无此调用则不注入，避免启动崩。
    @Redirect(method = "updatePlayerStatus", require = 0, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/entity/EntityAccess;)Lnet/minecraft/core/SectionPos;"))
    private static SectionPos mixinEnhance$useFreeCamCenterInUpdateStatus(EntityAccess entity) {
        return useFreeCamCenter(entity);
    }

    private static SectionPos useFreeCamCenter(EntityAccess entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            SectionPos cameraSectionPos = FreeCamTracker.getSectionPosOrNull(serverPlayer.getUUID());
            if (cameraSectionPos != null) {
                return cameraSectionPos;
            }
        }
        return SectionPos.of(entity);
    }
}
