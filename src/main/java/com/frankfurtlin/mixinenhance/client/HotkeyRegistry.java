package com.frankfurtlin.mixinenhance.client;

import com.frankfurtlin.mixinenhance.MixinEnhanceConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * 快捷键注册表：把「注册按键 → 派发按键 → 配置总开关门控 → 切换后提示 → 配置关闭时收尾」
 * 收敛成一处，新增快捷键只需调用一次 {@link #register}。
 *
 * <p>语义（沿用模组既有约定）：配置项是<b>总开关</b>——配置关闭时按键完全无反应，且特性立即失效
 * （由 {@code onDisabled} 负责把运行时状态收干净）。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/15
 */
public final class HotkeyRegistry {
    /** 按键分类（对应语言文件 category.mixinenhance.main）。 */
    private static final KeyMapping.Category CATEGORY =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("mixinenhance", "main"));

    /** 已注册的绑定，保持注册顺序（即控制界面里的排序）。 */
    private static final Map<String, Binding> BINDINGS = new LinkedHashMap<>();

    private static boolean ticking;

    private HotkeyRegistry() {
    }

    private record Binding(String id, KeyMapping key, String messageKey, BooleanSupplier enabled,
                           Runnable toggle, BooleanSupplier active, Runnable onDisabled) {
    }

    /** 开始监听客户端 tick（模组初始化时调用一次）。 */
    public static void init() {
        if (ticking) {
            return;
        }
        ticking = true;
        ClientTickEvents.END_CLIENT_TICK.register(HotkeyRegistry::tick);
    }

    /**
     * 注册一个快捷键。
     *
     * @param id              快捷键 id（{@code @HotkeyHint} 用它关联配置项）
     * @param translationKey  按键名翻译键（{@code key.mixinenhance.*}）
     * @param messageKey      切换提示的翻译键后缀（{@code message.mixinenhance.<messageKey>.on/.off}）
     * @param enabled         配置总开关是否开启
     * @param toggle          切换动作
     * @param active          当前是否处于开启状态（仅用于日志与提示）
     * @param onDisabled      配置被关闭时的收尾动作（清掉运行时状态）
     */
    public static void register(String id, String translationKey, String messageKey, BooleanSupplier enabled,
                                Runnable toggle, BooleanSupplier active, Runnable onDisabled) {
        KeyMapping key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            translationKey, InputConstants.Type.KEYBOARD, defaultKeyOf(id), CATEGORY, BINDINGS.size()));
        BINDINGS.put(id, new Binding(id, key, messageKey, enabled, toggle, active, onDisabled));
    }

    /** 默认按键：新功能在这里登记即可（0 表示未分配）。 */
    private static int defaultKeyOf(String id) {
        return switch (id) {
            case "freecam" -> InputConstants.KEY_F6;
            case "gamma" -> InputConstants.KEY_F7;
            default -> InputConstants.UNKNOWN.getValue();
        };
    }

    private static void tick(Minecraft client) {
        for (Binding binding : BINDINGS.values()) {
            // 配置关闭时：按键不予响应，并把运行时状态收干净
            if (!binding.enabled().getAsBoolean()) {
                if (binding.active().getAsBoolean()) {
                    binding.onDisabled().run();
                }
                // 仍要消费掉点击，避免按键在「配置关闭期间」被积压后突然生效
                while (binding.key().consumeClick()) {
                    // 配置关闭，按了也不做任何事
                }
                continue;
            }
            while (binding.key().consumeClick()) {
                binding.toggle().run();
                showToggleMessage(binding.messageKey(), binding.active().getAsBoolean());
            }
        }
    }

    /** 当前绑定按键的可读名称（配置界面与文档用），未注册时返回「未绑定」。 */
    public static String keyNameOf(String id) {
        Binding binding = BINDINGS.get(id);
        if (binding == null) {
            return "-";
        }
        return binding.key().getTranslatedKeyMessage().getString();
    }

    /** 已注册的快捷键 id 列表（顺序即界面顺序）。 */
    public static List<String> ids() {
        return new ArrayList<>(BINDINGS.keySet());
    }

    /** 快捷键切换后在物品栏上方提示当前状态（文案见 lang 里的 message.mixinenhance.*）。 */
    private static void showToggleMessage(String messageKey, boolean enabled) {
        Minecraft.getInstance().gui.hud.setOverlayMessage(
            Component.translatable("message.mixinenhance." + messageKey + (enabled ? ".on" : ".off")), false);
    }

    /** 便捷方法：读取当前配置（避免各功能重复 import 配置门面）。 */
    public static boolean configEnabled(java.util.function.Function<com.frankfurtlin.mixinenhance.config.ModMenuConfig, Boolean> getter) {
        return getter.apply(MixinEnhanceConfig.get());
    }
}
