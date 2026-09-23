package com.frankfurtlin.mixinenhance.mixin.enchantment;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenmixin {
    // 消除附魔经验上限
    @ModifyConstant(method = "extractLabels", constant = @Constant(intValue = 40))
    public int modifyLimit(int limit) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeAnvilLimit){
            return Integer.MAX_VALUE;
        }
        return 40;
    }
}
