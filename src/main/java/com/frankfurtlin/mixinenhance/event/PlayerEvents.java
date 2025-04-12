package com.frankfurtlin.mixinenhance.event;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2025/4/12 9:34
 */
public class PlayerEvents {
    public static void onPlayerJoined(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        if (MixinEnhanceClient.getConfig().defaultModuleConfig.enableJoinWithItems) {
            ServerPlayerEntity player = handler.player;
            if (!hasElytra(player)) {
                ItemStack elytra = new ItemStack(Items.ELYTRA);
                player.giveItemStack(elytra);
                ItemStack fireworkRocket = new ItemStack(Items.FIREWORK_ROCKET, 64*2);
                player.giveItemStack(fireworkRocket);
                player.sendMessage(Text.literal("欢迎加入服务器！这是你的初始物资！"), false);
            }
        }
    }

    // 检查玩家是否拥有鞘翅（背包、装备栏、副手）
    private static boolean hasElytra(ServerPlayerEntity player) {
        // 遍历主背包（包括快捷栏）
        for (ItemStack stack : player.getInventory().getMainStacks()) {
            if (stack.isOf(Items.ELYTRA)) return true;
        }

        // 检查装备中的鞘翅
        if (player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
            return true;
        }

        // 检查副手
        return player.getOffHandStack().isOf(Items.ELYTRA);
    }
}
