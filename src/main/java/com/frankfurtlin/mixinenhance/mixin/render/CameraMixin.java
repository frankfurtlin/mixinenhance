package com.frankfurtlin.mixinenhance.mixin.render;

import com.frankfurtlin.mixinenhance.client.FreeCamState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 自由视角：接管相机对齐逻辑。
 *
 * 参考成熟实现（Zergatul/freecam，26.3 分支），有三个关键点：
 * 1) 在 alignWithEntity 中取消原版对齐逻辑，而不是在原版算完后再覆盖，避免重复计算；
 * 2) 置 detached = true，让相机真正标记为「脱离实体」；
 * 3) 让渲染状态里的 isSpectator 返回 true，使渲染管线按旁观者路径处理。
 *
 * 缺少后两点时，渲染器仍把相机当作贴在玩家眼睛上处理，剔除与渲染路径都不对，
 * 表现为自由视角下持续卡顿。
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private boolean detached;

    // 用 aliases 精确指定目标方法，避免与 setPosition(Vec3) 重载混淆
    @Shadow(aliases = "Lnet/minecraft/client/Camera;setRotation(FF)V")
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow(aliases = "Lnet/minecraft/client/Camera;setPosition(DDD)V")
    protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "alignWithEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isPassenger()Z", ordinal = 0),
        cancellable = true)
    private void mixinEnhance$alignWithFreeCam(float partialTicks, CallbackInfo ci) {
        if (!FreeCamState.isActive()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        this.detached = true;
        setRotation(minecraft.player.getYRot(), minecraft.player.getXRot());
        setPosition(FreeCamState.getX(), FreeCamState.getY(), FreeCamState.getZ());
        ci.cancel();
    }

    @ModifyExpressionValue(method = "extractRenderState",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
    private boolean mixinEnhance$forceSpectator(boolean original) {
        return FreeCamState.isActive() || original;
    }
}
