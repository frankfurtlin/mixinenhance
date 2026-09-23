package com.frankfurtlin.mixinenhance.event;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/12 9:34
 */
public class PlayerEvents {
    public static void onPlayerJoined(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        if (MixinEnhanceClient.getConfig().defaultModuleConfig.enableJoinWithItems) {
            ServerPlayer player = handler.player;
            if (!hasElytra(player)) {
                ItemStack elytra = new ItemStack(Items.ELYTRA);
                player.addItem(elytra);
                ItemStack fireworkRocket = new ItemStack(Items.FIREWORK_ROCKET, 64*2);
                player.addItem(fireworkRocket);
                player.sendSystemMessage(Component.literal("欢迎加入服务器！这是你的初始物资！"));
            }
        }
    }

    // 检查玩家是否拥有鞘翅（背包、装备栏、副手）
    private static boolean hasElytra(ServerPlayer player) {
        // 遍历主背包（包括快捷栏）
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(Items.ELYTRA)) return true;
        }

        // 检查装备中的鞘翅
        if (player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            return true;
        }

        // 检查副手
        return player.getOffhandItem().is(Items.ELYTRA);
    }
}
