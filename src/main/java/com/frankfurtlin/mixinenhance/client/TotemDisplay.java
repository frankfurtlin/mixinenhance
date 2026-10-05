package com.frankfurtlin.mixinenhance.client;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 图腾计数：经验条上方用「不死图腾物品图标 + 数量」展示背包与副手中不死图腾的总数。
 * 位置算法参考 TotemCounter（uku3lig/totemcounter）。
 *
 * 26.3 / Fabric API 27 中 HudRenderCallback 已移除，改用 HudElement 注册模式。
 */
public class TotemDisplay {
    private static final int COLOR = 0xFFFFFFFF;

    public static void init() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("mixinenhance", "totem"), (context, tickCounter) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null) {
                return;
            }

            if (MixinEnhanceClient.getConfig().entityModuleConfig.playerConfig.enableTotemCounter) {
                renderTotemCounter(context, minecraft);
            }
        });
    }

    /**
     * 图腾计数：显示在原版人物等级（经验条）上方，图标 + 数量（数量在图标右侧垂直居中）。
     */
    private static void renderTotemCounter(GuiGraphicsExtractor context, Minecraft minecraft) {
        Player player = minecraft.player;
        int count = countTotems(player);
        if (count == 0) {
            return;
        }

        int x = context.guiWidth() / 2 - 8;
        int y = context.guiHeight() - 38 - minecraft.font.lineHeight;
        if (player.experienceLevel > 0) {
            y -= 6;
        }

        context.item(new ItemStack(Items.TOTEM_OF_UNDYING), x, y);
        int textY = y + (16 - minecraft.font.lineHeight) / 2;
        context.text(minecraft.font, String.valueOf(count), x + 16, textY, COLOR);
    }

    /** 统计背包 + 副手中不死图腾的总数。 */
    private static int countTotems(Player player) {
        int count = 0;
        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.getNonEquipmentItems()) {
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                count += stack.getCount();
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(Items.TOTEM_OF_UNDYING)) {
            count += offhand.getCount();
        }
        return count;
    }
}
