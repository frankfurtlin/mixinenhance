package com.frankfurtlin.mixinenhance;

import com.frankfurtlin.mixinenhance.event.BlockEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/10 13:44
 */
public class MixinEnhance implements ModInitializer {
    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.BEFORE.register(BlockEvents::onBlockBreak);
    }
}
