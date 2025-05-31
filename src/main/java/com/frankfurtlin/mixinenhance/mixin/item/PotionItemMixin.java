package com.frankfurtlin.mixinenhance.mixin.item;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.item.Item;
import net.minecraft.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin {
    // 药水堆叠至64
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static Item.Settings modifyStackSize(Item.Settings settings) {
        if (MixinEnhanceClient.getConfig().itemModuleConfig.canStackTo64) {
            return settings.maxCount(64);
        }
        return settings.maxCount(1);
    }
}