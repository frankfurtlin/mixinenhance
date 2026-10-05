package com.frankfurtlin.mixinenhance;

import com.frankfurtlin.mixinenhance.client.FreeCamState;
import com.frankfurtlin.mixinenhance.client.TotemDisplay;
import com.frankfurtlin.mixinenhance.config.ModMenuConfig;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
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
        // 自由视角切换键（默认 F4）
        // 26.3：KeyBindingHelper 改名 KeyMappingHelper，且按键分类由 String 改为 KeyMapping.Category
        KeyMapping freeCamKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.mixinenhance.freecam",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_F4,
            KEY_CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (freeCamKey.consumeClick()) {
                if (getConfig().entityModuleConfig.playerConfig.enableFreeCam) {
                    FreeCamState.toggle();
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
    }
}
