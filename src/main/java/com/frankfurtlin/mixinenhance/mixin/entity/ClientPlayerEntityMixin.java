package com.frankfurtlin.mixinenhance.mixin.entity;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Shadow
    @Final
    protected MinecraftClient client;

    // 玩家在地狱门中不强制关闭背包页面
    @Redirect(method = "tickNausea", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"))
    private Screen updateNauseaScreen(MinecraftClient instance) {
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.canOpenGuiInPortal){
            return null;
        }
        return this.client.currentScreen;
    }
}