package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 物品自动补货（StackRefill）：当前手持物品用完时，自动从背包补充同种物品，
 * 方便连续吃东西、放置方块、射箭等。仅在服务端生效，避免双端重复处理。
 */
@Mixin(Player.class)
public abstract class PlayerStackRefillMixin {
    @Unique
    private Item mixinEnhance$lastHeldItem = null;

    @Unique
    private int mixinEnhance$lastSelectedSlot = -1;

    @Inject(method = "tick", at = @At("HEAD"))
    private void stackRefill(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!MixinEnhanceClient.getConfig().defaultModuleConfig.enableStackRefill) {
            return;
        }
        if (self.level().isClientSide()) {
            return;
        }

        Inventory inventory = self.getInventory();
        int selected = inventory.getSelectedSlot();

        // 玩家切换了快捷栏槽位，重置"待补货"状态，避免误补
        if (selected != mixinEnhance$lastSelectedSlot) {
            mixinEnhance$lastSelectedSlot = selected;
            mixinEnhance$lastHeldItem = null;
            return;
        }

        ItemStack mainHand = self.getMainHandItem();
        if (!mainHand.isEmpty()) {
            // 手上还有物品，记录其类型作为下次补货依据
            mixinEnhance$lastHeldItem = mainHand.getItem();
            return;
        }

        if (mixinEnhance$lastHeldItem == null) {
            return;
        }

        // 手上已空，从背包找同种物品补到手上
        int slot = inventory.findSlotMatchingItem(new ItemStack(mixinEnhance$lastHeldItem));
        if (slot >= 0) {
            ItemStack stack = inventory.getItem(slot);
            self.setItemInHand(InteractionHand.MAIN_HAND, stack);
            inventory.setItem(slot, ItemStack.EMPTY);
        }
        mixinEnhance$lastHeldItem = null;
    }
}
