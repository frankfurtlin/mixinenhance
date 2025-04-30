package com.frankfurtlin.mixinenhance.mixin;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import com.mojang.serialization.Lifecycle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntegratedServerLoader.class)
public abstract class IntegratedServerLoaderMixin {

    @Shadow protected abstract void start(LevelStorage.Session session, SaveLoader saveLoader, ResourcePackManager dataPackManager, Runnable onCancel);

    // 消除加载世界时的实验性特性弹窗，创建世界
    @Inject(method = "tryLoad", at = @At("HEAD"), cancellable = true)
    private static void removeWarning(MinecraftClient client, CreateWorldScreen parent, Lifecycle lifecycle, Runnable loader, boolean bypassWarnings, CallbackInfo ci){
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.removeExperimentalWarning){
            loader.run();
            ci.cancel();
        }
    }

    // 消除加载世界时的实验性特性弹窗，进入世界
    @Inject(method = "checkBackupAndStart", at = @At("HEAD"), cancellable = true)
    private void removeWarning2(LevelStorage.Session session, SaveLoader saveLoader, ResourcePackManager dataPackManager, Runnable onCancel, CallbackInfo ci){
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.removeExperimentalWarning){
            this.start(session, saveLoader, dataPackManager, onCancel);
            ci.cancel();
        }
    }



}