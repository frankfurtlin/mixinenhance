package com.frankfurtlin.mixinenhance.mixin.enchantment;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilMenu.class)
public abstract class AnvilScreenHandlerMixin {
    // 消除附魔经验上限
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40), require = 0)
    public int modifyLimit(int limit) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.enchantmentConfig.removeAnvilLimit){
            return Integer.MAX_VALUE;
        }
        return 40;
    }
}