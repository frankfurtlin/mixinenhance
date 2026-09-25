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
 * 这里只替换 ChunkMap.move 中用于计算追踪视图的位置，玩家实体本身不移动。
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Redirect(method = "move", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/entity/EntityAccess;)Lnet/minecraft/core/SectionPos;"))
    private static SectionPos mixinEnhance$useFreeCamCenter(EntityAccess entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            SectionPos cameraSectionPos = FreeCamTracker.getSectionPosOrNull(serverPlayer.getUUID());
            if (cameraSectionPos != null) {
                return cameraSectionPos;
            }
        }
        return SectionPos.of(entity);
    }
}
