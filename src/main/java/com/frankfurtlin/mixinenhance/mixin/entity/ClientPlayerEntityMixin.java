package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    // 玩家在地狱门中不强制关闭背包页面
    // 26.3 中当前屏幕改由 Minecraft.gui 管理，通过 Gui.screen() 访问
    @Redirect(method = "handlePortalTransitionEffect",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;"))
    private Screen updateNauseaScreen(Gui instance) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.canOpenGuiInPortal){
            return null;
        }
        return instance.screen();
    }
}
