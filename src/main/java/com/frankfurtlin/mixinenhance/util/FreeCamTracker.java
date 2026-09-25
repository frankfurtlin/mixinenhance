package com.frankfurtlin.mixinenhance.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import java.util.UUID;

/**
 * 自由视角状态的「服务端可读副本」。
 *
 * 这个类刻意不引用任何客户端类（net.minecraft.client.*），
 * 使服务端的 ChunkMapMixin 可以安全读取；在专用服务器上永远保持未激活状态。
 * 客户端的 FreeCamState 负责把状态同步到这里。
 *
 * 关键：区块加载中心采用「节流式迁移」——若相机每跨一个区块就迁移一次中心，
 * 会不断触发一整圈区块的加载与卸载，开销极大且表现为持续卡顿。
 * 这里让中心在相机偏离达到阈值（若干区块）后才迁移一次，附近观察时中心保持不动。
 */
public class FreeCamTracker {
    /** 加载中心迁移阈值（区块数）：相机偏离中心超过该距离才迁移一次 */
    private static final int MIGRATE_THRESHOLD_CHUNKS = 4;

    private static volatile boolean active = false;
    private static volatile UUID playerId = null;
    private static volatile double x;
    private static volatile double y;
    private static volatile double z;
    private static volatile int centerChunkX = Integer.MIN_VALUE;
    private static volatile int centerChunkZ = Integer.MIN_VALUE;

    public static void setActive(boolean value, UUID uuid, double posX, double posY, double posZ) {
        active = value;
        playerId = value ? uuid : null;
        x = posX;
        y = posY;
        z = posZ;
        centerChunkX = chunkOf(posX);
        centerChunkZ = chunkOf(posZ);
    }

    /**
     * 相机位置变化时调用；只有偏离当前加载中心达到阈值才真正迁移中心。
     */
    public static void updatePosition(double posX, double posY, double posZ) {
        if (!active) {
            return;
        }
        int currentChunkX = chunkOf(posX);
        int currentChunkZ = chunkOf(posZ);
        if (Math.abs(currentChunkX - centerChunkX) < MIGRATE_THRESHOLD_CHUNKS &&
            Math.abs(currentChunkZ - centerChunkZ) < MIGRATE_THRESHOLD_CHUNKS) {
            return; // 仍在阈值范围内，保持加载中心不变
        }
        centerChunkX = currentChunkX;
        centerChunkZ = currentChunkZ;
        x = posX;
        y = posY;
        z = posZ;
    }

    public static void clear() {
        active = false;
        playerId = null;
        centerChunkX = Integer.MIN_VALUE;
        centerChunkZ = Integer.MIN_VALUE;
    }

    public static boolean isActive() {
        return active;
    }

    /**
     * 若指定玩家正处于自由视角，返回用作区块加载中心的 SectionPos；否则返回 null。
     */
    public static SectionPos getSectionPosOrNull(UUID uuid) {
        if (!active || playerId == null || !playerId.equals(uuid)) {
            return null;
        }
        return SectionPos.of(BlockPos.containing(x, y, z));
    }

    private static int chunkOf(double coordinate) {
        return (int) Math.floor(coordinate / 16.0);
    }
}
