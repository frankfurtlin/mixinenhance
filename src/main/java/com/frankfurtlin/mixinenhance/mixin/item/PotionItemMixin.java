package com.frankfurtlin.mixinenhance.mixin.item;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {
    // 药水堆叠至64
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static Item.Properties modifyStackSize(Item.Properties settings) {
        if (MixinEnhanceClient.getConfig().itemModuleConfig.canStackTo64) {
            return settings.stacksTo(64);
        }
        return settings.stacksTo(1);
    }
}