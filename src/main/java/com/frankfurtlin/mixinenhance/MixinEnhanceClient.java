package com.frankfurtlin.mixinenhance;

import com.frankfurtlin.mixinenhance.config.ModMenuConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/10 13:44
 */
public class MixinEnhanceClient implements ClientModInitializer {
    private static final ConfigHolder<ModMenuConfig> configHolder;

    static {
        configHolder = AutoConfig.register(ModMenuConfig.class, JanksonConfigSerializer::new);
    }


    public static ModMenuConfig getConfig() {
        if (configHolder == null) {
            throw new IllegalStateException("ConfigHolder is not initialized");
        }
        return configHolder.getConfig();
    }
    @Override
    public void onInitializeClient() {
    }
}
