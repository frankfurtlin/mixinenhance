package com.frankfurtlin.mixinenhance.config.modMenuMuduleConfig;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 7:10
 */
public class BlockModuleConfig {
    @ConfigEntry.Gui.Tooltip
    public boolean enableSpawnerDropWithSilkTouch = false;           // 是否启用刷怪笼被精准采集掉落
}
