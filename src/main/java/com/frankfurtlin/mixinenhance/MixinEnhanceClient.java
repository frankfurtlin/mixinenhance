package com.frankfurtlin.mixinenhance;

import com.frankfurtlin.mixinenhance.client.ContainerAutoSort;
import com.frankfurtlin.mixinenhance.client.DynamicLightHandler;
import com.frankfurtlin.mixinenhance.client.FreeCamState;
import com.frankfurtlin.mixinenhance.client.GammaState;
import com.frankfurtlin.mixinenhance.client.TotemDisplay;
import com.frankfurtlin.mixinenhance.config.ModMenuConfig;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/10 13:44
 */
public class MixinEnhanceClient implements ClientModInitializer {
    /** 本模组的按键分类，对应语言文件的 category.mixinenhance.main */
    private static final KeyMapping.Category KEY_CATEGORY =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath("mixinenhance", "main"));

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
        // 自由视角切换键（默认 F6；原版 F6 只有调试组合键 F3+F6，单独按不冲突）
        // 26.3：KeyBindingHelper 改名 KeyMappingHelper，且按键分类由 String 改为 KeyMapping.Category
        // 末尾的 0/1 是分类内的排序权重（KeyMapping 的第 5 个参数）：同类目下权重小的排在前面，
        // 不传（默认 0）时会退化成按翻译后的名称排序，中文里「亮度」会排在「自由」前面。
        KeyMapping freeCamKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.mixinenhance.freecam",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_F6,
            KEY_CATEGORY,
            0));

        // 亮度调节切换键（默认 F7；F4 有旁观者着色器开关、F5 是原版「切换视角」，都避开）
        KeyMapping gammaKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.mixinenhance.gamma",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_F7,
            KEY_CATEGORY,
            1));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 两个快捷键遵循同一套语义：配置项是总开关，配置关掉时按了也不会有任何反应。
            while (freeCamKey.consumeClick()) {
                if (getConfig().playerConfig.enableFreeCam) {
                    FreeCamState.toggle();
                    showToggleMessage("freecam", FreeCamState.isActive());
                }
            }
            // 配置被关掉（改配置/换存档）时立刻退出自由视角，避免状态残留
            if (!getConfig().playerConfig.enableFreeCam && FreeCamState.isActive()) {
                FreeCamState.toggle();
            }
            // 同理：亮度配置关掉时清掉本次游戏的开关状态（全亮立即失效，重新开启后默认仍是原版亮度）
            if (!getConfig().playerConfig.enableGamma) {
                GammaState.reset();
            }

            while (gammaKey.consumeClick()) {
                if (getConfig().playerConfig.enableGamma) {
                    showToggleMessage("gamma", GammaState.toggle());
                }
            }
        });

        // 自由视角期间禁止一切交互（左键破坏/攻击、右键使用方块/物品/实体），避免误操作
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) ->
            FreeCamState.isActive() ? InteractionResult.FAIL : InteractionResult.PASS);
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
            FreeCamState.isActive() ? InteractionResult.FAIL : InteractionResult.PASS);
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
            FreeCamState.isActive() ? InteractionResult.FAIL : InteractionResult.PASS);
        UseItemCallback.EVENT.register((player, level, hand) ->
            FreeCamState.isActive() ? InteractionResult.FAIL : InteractionResult.PASS);
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
            FreeCamState.isActive() ? InteractionResult.FAIL : InteractionResult.PASS);

        // 图腾计数显示（经验条上方图标 + 数量）
        TotemDisplay.init();

        // 动态照明：每客户端 tick 刷新动态光源列表（手持发光物照亮周围）
        ClientTickEvents.END_CLIENT_TICK.register(DynamicLightHandler::update);

        // 打开背包/容器时自动整理（合并相同物品并按物品顺序排序）
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
            ContainerAutoSort.onScreenInit(screen));
        ClientTickEvents.END_CLIENT_TICK.register(ContainerAutoSort::onClientTick);
    }

    /** 快捷键切换后在物品栏上方提示当前状态（文案见 lang 里的 message.mixinenhance.*）。 */
    private static void showToggleMessage(String feature, boolean enabled) {
        Minecraft.getInstance().gui.hud.setOverlayMessage(
            Component.translatable("message.mixinenhance." + feature + (enabled ? ".on" : ".off")), false);
    }
}
