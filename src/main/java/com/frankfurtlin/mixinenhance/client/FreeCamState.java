package com.frankfurtlin.mixinenhance.client;

import com.frankfurtlin.mixinenhance.util.FreeCamTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

/**
 * 自由视角（Freecam）的客户端状态与移动逻辑。
 *
 * 开启后相机脱离玩家身体独立移动，玩家身体留在原地（由 KeyboardInputMixin 清零移动输入实现）。
 * 朝向仍由鼠标控制（沿用玩家的视角方向），因此无需在此维护 yaw / pitch。
 */
public class FreeCamState {
    private static boolean active = false;
    private static double x;
    private static double y;
    private static double z;

    public static boolean isActive() {
        return active;
    }

    public static double getX() {
        return x;
    }

    public static double getY() {
        return y;
    }

    public static double getZ() {
        return z;
    }

    /** 切换自由视角；开启时从玩家眼睛位置起飞 */
    public static void toggle() {
        Minecraft minecraft = Minecraft.getInstance();
        if (active) {
            active = false;
            FreeCamTracker.clear();
            return;
        }
        if (minecraft.player == null) {
            return;
        }
        x = minecraft.player.getX();
        y = minecraft.player.getEyeY();
        z = minecraft.player.getZ();
        active = true;
        // 同步给服务端可读副本，使区块加载跟随相机
        FreeCamTracker.setActive(true, minecraft.player.getUUID(), x, y, z);
    }

    /**
     * 根据玩家输入移动自由相机。
     * 移动方向基于玩家当前视线方向，因此「看向哪就往哪飞」；空格上升、潜行下降、疾跑加速。
     */
    public static void update(Minecraft minecraft, Input input) {
        if (!active || minecraft.player == null) {
            return;
        }

        int forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
        int strafe = (input.right() ? 1 : 0) - (input.left() ? 1 : 0);
        int vertical = (input.jump() ? 1 : 0) - (input.shift() ? 1 : 0);

        if (forward == 0 && strafe == 0 && vertical == 0) {
            return;
        }

        // 速度偏快会让大量新区块连续进入视野、持续触发渲染编译而卡顿，故取值较保守
        float speed = input.sprint() ? 0.6F : 0.25F;

        // 前向：直接用玩家视线向量（含俯仰）
        Vec3 look = minecraft.player.getLookAngle();
        // 水平右向量：由偏航角旋转得到（与视线保持垂直，且不随俯仰倾斜）
        double yawRad = Math.toRadians(minecraft.player.getYRot());
        double rightX = -Math.cos(yawRad);
        double rightZ = -Math.sin(yawRad);

        x += (look.x * forward + rightX * strafe) * speed;
        y += (look.y * forward + vertical) * speed;
        z += (look.z * forward + rightZ * strafe) * speed;
        // 持续同步位置，让服务端保持相机周围的区块加载
        FreeCamTracker.updatePosition(x, y, z);
    }
}
