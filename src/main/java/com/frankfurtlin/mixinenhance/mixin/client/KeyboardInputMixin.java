package com.frankfurtlin.mixinenhance.mixin.client;

import com.frankfurtlin.mixinenhance.client.FreeCamState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 自由视角开启时：把移动输入交给自由相机，并清空玩家自身的移动，使身体留在原地。
 */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void mixinEnhance$freeCamInput(CallbackInfo ci) {
        if (!FreeCamState.isActive()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || this.keyPresses == null) {
            return;
        }

        Input original = this.keyPresses;
        // 相机照常响应全部输入（含空格上升、潜行下降）
        FreeCamState.update(minecraft, original);

        // 屏蔽跳跃与潜行：Input 是 record 不可改字段，故重建一个 jump/shift 为 false 的实例，
        // 否则身体会跟着相机一起上升、下潜
        this.keyPresses = new Input(
            original.forward(), original.backward(), original.left(), original.right(),
            false, false, original.sprint());

        // 清空水平移动向量，身体原地不动
        this.moveVector = new Vec2(0.0F, 0.0F);
    }
}
