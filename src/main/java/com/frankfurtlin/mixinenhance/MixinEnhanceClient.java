package com.frankfurtlin.mixinenhance;

import com.frankfurtlin.mixinenhance.client.ContainerAutoSort;
import com.frankfurtlin.mixinenhance.client.DynamicLightHandler;
import com.frankfurtlin.mixinenhance.client.FreeCamState;
import com.frankfurtlin.mixinenhance.client.GammaState;
import com.frankfurtlin.mixinenhance.client.HotkeyConfigScreen;
import com.frankfurtlin.mixinenhance.client.HotkeyRegistry;
import com.frankfurtlin.mixinenhance.client.TotemDisplay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/10 13:44
 */
public class MixinEnhanceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 快捷键：注册与派发统一走 HotkeyRegistry（新增功能只需注册一行）
        HotkeyRegistry.init();
        HotkeyRegistry.register(
            "freecam", "key.mixinenhance.freecam", "freecam",
            () -> MixinEnhanceConfig.get().playerConfig.enableFreeCam,
            FreeCamState::toggle, FreeCamState::isActive, FreeCamState::toggle);
        HotkeyRegistry.register(
            "gamma", "key.mixinenhance.gamma", "gamma",
            () -> MixinEnhanceConfig.get().playerConfig.enableGamma,
            GammaState::toggle, GammaState::isEnabled, GammaState::reset);

        // 配置界面里给「有快捷键的开关」补一行「当前按键：F6」提示
        HotkeyConfigScreen.register();

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
}
