package com.frankfurtlin.mixinenhance;

import com.frankfurtlin.mixinenhance.event.Events;
import net.fabricmc.api.ModInitializer;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/10 13:44
 */
public class MixinEnhance implements ModInitializer {
    @Override
    public void onInitialize() {
        Events.initialize();
    }
}
