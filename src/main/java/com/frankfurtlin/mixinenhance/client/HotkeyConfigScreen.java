package com.frankfurtlin.mixinenhance.client;

import com.frankfurtlin.mixinenhance.config.HotkeyHint;
import com.frankfurtlin.mixinenhance.config.ModMenuConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.gui.registry.api.GuiProvider;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 把「有快捷键的配置项」渲染成「原开关 + 一行当前按键提示」。
 *
 * <p>Cloth Config 默认只按字段类型生成条目，看不到绑定的是哪个键（而且改绑后会变）。这里给带
 * {@link HotkeyHint} 的字段注册一个 GUI 提供者，在原开关下面补一行 {@code 当前按键：F6}。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/15
 */
public final class HotkeyConfigScreen {
    private HotkeyConfigScreen() {
    }

    /** 注册 GUI 提供者（客户端初始化时调用一次）。 */
    public static void register() {
        try {
            AutoConfigClient.getGuiRegistry(ModMenuConfig.class)
                .registerAnnotationProvider(new HotkeyHintProvider(), HotkeyHint.class);
        } catch (Throwable e) {
            // 注册失败只影响「配置页显示按键」这一项装饰，绝不能让客户端启动失败
            com.frankfurtlin.mixinenhance.MixinEnhance.LOGGER
                .warn("配置页快捷键提示未注册：{}", e.toString());
        }
    }

    /** 只处理带 {@link HotkeyHint} 的布尔开关。 */
    private static final class HotkeyHintProvider implements GuiProvider {
        @Override
        public List<AbstractConfigListEntry> get(String i18n, Field field, Object config, Object defaults,
                                                 GuiRegistryAccess registry) {
            HotkeyHint hint = field.getAnnotation(HotkeyHint.class);
            List<AbstractConfigListEntry> entries = new ArrayList<>(2);
            try {
                boolean value = field.getBoolean(config);
                boolean defaultValue = field.getBoolean(defaults);
                var toggle = ConfigEntryBuilder.create()
                    .startBooleanToggle(Component.translatable(i18n), value)
                    .setDefaultValue(defaultValue)
                    .setSaveConsumer(v -> setBoolean(field, config, v));
                String tooltipKey = i18n + ".@Tooltip";
                if (Language.getInstance().has(tooltipKey)) {
                    toggle.setTooltip(Component.translatable(tooltipKey));
                }
                entries.add(toggle.build());
            } catch (Throwable e) {
                // 反射失败时退化成「只有提示行」，绝不让配置界面整体打不开
                com.frankfurtlin.mixinenhance.MixinEnhance.LOGGER
                    .warn("配置项 {} 的开关渲染失败：{}", i18n, e.toString());
            }
            entries.add(ConfigEntryBuilder.create().startTextDescription(hotkeyLine(hint)).build());
            return entries;
        }

        private static Component hotkeyLine(HotkeyHint hint) {
            String key;
            try {
                key = HotkeyRegistry.keyNameOf(hint.value());
            } catch (Throwable e) {
                key = "-";
            }
            return Component.translatable("text.autoconfig.mixinenhance.hotkey", key);
        }

        private static void setBoolean(Field field, Object config, boolean value) {
            try {
                field.setBoolean(config, value);
            } catch (Exception ignored) {
                // 字段不可写时交给 AutoConfig 的默认流程处理
            }
        }
    }
}
