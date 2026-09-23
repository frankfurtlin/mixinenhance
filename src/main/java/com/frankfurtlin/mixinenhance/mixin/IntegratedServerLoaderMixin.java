package com.frankfurtlin.mixinenhance.mixin;

import com.frankfurtlin.mixinenhance.MixinEnhanceClient;
import com.mojang.serialization.Lifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldOpenFlows.class)
public abstract class IntegratedServerLoaderMixin {

    @Shadow protected abstract void openWorldLoadBundledResourcePack(LevelStorageSource.LevelStorageAccess session, WorldStem saveLoader, PackRepository dataPackManager, Runnable onCancel);

    // 消除加载世界时的实验性特性弹窗，创建世界
    @Inject(method = "confirmWorldCreation", at = @At("HEAD"), cancellable = true)
    private static void removeWarning(Minecraft client, CreateWorldScreen parent, Lifecycle lifecycle, Runnable loader, boolean bypassWarnings, CallbackInfo ci){
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.removeExperimentalWarning){
            loader.run();
            ci.cancel();
        }
    }

    // 消除加载世界时的实验性特性弹窗，进入世界
    @Inject(method = "openWorldCheckWorldStemCompatibility", at = @At("HEAD"), cancellable = true)
    private void removeWarning2(LevelStorageSource.LevelStorageAccess session, WorldStem saveLoader, PackRepository dataPackManager, Runnable onCancel, CallbackInfo ci){
        if(MixinEnhanceClient.getConfig().defaultModuleConfig.removeExperimentalWarning){
            this.openWorldLoadBundledResourcePack(session, saveLoader, dataPackManager, onCancel);
            ci.cancel();
        }
    }



}