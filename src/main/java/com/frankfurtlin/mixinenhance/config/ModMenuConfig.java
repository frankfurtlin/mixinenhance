package com.frankfurtlin.mixinenhance.config;

import com.frankfurtlin.mixinenhance.ConfigMigrations;
import com.frankfurtlin.mixinenhance.config.section.BlockConfig;
import com.frankfurtlin.mixinenhance.config.section.EnchantTradeConfig;
import com.frankfurtlin.mixinenhance.config.section.EntityConfig;
import com.frankfurtlin.mixinenhance.config.section.FishingConfig;
import com.frankfurtlin.mixinenhance.config.section.GeneralConfig;
import com.frankfurtlin.mixinenhance.config.section.ItemConfig;
import com.frankfurtlin.mixinenhance.config.section.PlayerConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * 配置根：按功能域划分为 7 个标签页。
 * <p>标签页顺序即下方字段声明顺序：常规 / 玩家 / 方块与挖掘 / 物品与装备 / 实体与怪物 / 附魔与交易 / 钓鱼。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/12 7:09
 */
@Config(name = "mixinenhance")
@SuppressWarnings("FieldMayBeFinal")
public class ModMenuConfig implements ConfigData {
    /**
     * 配置结构版本号：由 {@link ConfigMigrations} 在读取配置前自动维护，用于把旧配置迁移到新结构。
     * 不在配置界面上显示（改字段名/移动字段时见 docs/开发规范.md 的迁移约定）。
     *
     * <p>注意：{@code @ConfigEntry.Category} 必须给上。没有归属标签页的字段会让 Cloth 额外生成一个
     * {@code category.default} 标签页（未翻译、内容为空），即使字段自己已经被 {@code Gui.Excluded} 隐藏。</p>
     */
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Excluded
    public int configVersion = ConfigMigrations.CURRENT_VERSION;

    // 常规
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public GeneralConfig generalConfig = new GeneralConfig();

    // 玩家
    @ConfigEntry.Category("player")
    @ConfigEntry.Gui.TransitiveObject
    public PlayerConfig playerConfig = new PlayerConfig();

    // 方块与挖掘
    @ConfigEntry.Category("block")
    @ConfigEntry.Gui.TransitiveObject
    public BlockConfig blockConfig = new BlockConfig();

    // 物品与装备
    @ConfigEntry.Category("item")
    @ConfigEntry.Gui.TransitiveObject
    public ItemConfig itemConfig = new ItemConfig();

    // 实体与怪物
    @ConfigEntry.Category("entity")
    @ConfigEntry.Gui.TransitiveObject
    public EntityConfig entityConfig = new EntityConfig();

    // 附魔与交易
    @ConfigEntry.Category("enchant")
    @ConfigEntry.Gui.TransitiveObject
    public EnchantTradeConfig enchantConfig = new EnchantTradeConfig();

    // 钓鱼
    @ConfigEntry.Category("fishing")
    @ConfigEntry.Gui.TransitiveObject
    public FishingConfig fishingConfig = new FishingConfig();
}
