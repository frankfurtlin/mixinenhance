package com.frankfurtlin.mixinenhance.config;

import com.frankfurtlin.mixinenhance.config.modMenuMuduleConfig.BlockModuleConfig;
import com.frankfurtlin.mixinenhance.config.modMenuMuduleConfig.DefaultModuleConfig;
import com.frankfurtlin.mixinenhance.config.modMenuMuduleConfig.EntityModuleConfig;
import com.frankfurtlin.mixinenhance.config.modMenuMuduleConfig.ItemModuleConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * 配置参考链接
 * <a href="https://shedaniel.gitbook.io/cloth-config/auto-config/annotations">...</a>
 */
@Config(name = "mixinenhance")
@SuppressWarnings("FieldMayBeFinal")
public class ModMenuConfig implements ConfigData {
    // 特性
    @ConfigEntry.Category("default")
    @ConfigEntry.Gui.TransitiveObject
    public DefaultModuleConfig defaultModuleConfig = new DefaultModuleConfig();

    // 物品
    @ConfigEntry.Category("item")
    @ConfigEntry.Gui.TransitiveObject
    public ItemModuleConfig itemModuleConfig = new ItemModuleConfig();

    // 方块
    @ConfigEntry.Category("block")
    @ConfigEntry.Gui.TransitiveObject
    public BlockModuleConfig blockModuleConfig = new BlockModuleConfig();

    // 实体
    @ConfigEntry.Category("entity")
    @ConfigEntry.Gui.TransitiveObject
    public EntityModuleConfig entityModuleConfig = new EntityModuleConfig();

}