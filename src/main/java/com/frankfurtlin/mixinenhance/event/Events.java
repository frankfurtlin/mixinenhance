package com.frankfurtlin.mixinenhance.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/12 18:43
 */
public class Events {
    public static void initialize() {
        // 玩家破坏方块
        PlayerBlockBreakEvents.BEFORE.register(BlockEvents::onBlockBreak);
        // 玩家加入世界
        ServerPlayConnectionEvents.JOIN.register(PlayerEvents::onPlayerJoined);
    }
}
