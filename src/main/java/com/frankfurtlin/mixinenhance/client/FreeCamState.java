package com.frankfurtlin.mixinenhance.client;

import com.frankfurtlin.mixinenhance.util.FreeCamTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

/**
 * 自由视角（Freecam）的客户端状态与移动逻辑。
 *
 * <p>开启后相机脱离玩家身体独立移动，玩家身体留在原地（由 KeyboardInputMixin 清零移动输入实现）。
 * 朝向仍由鼠标控制（沿用玩家的视角方向），因此无需在此维护 yaw / pitch。</p>
 *
 * <p>手感对齐原版观察者/创造飞行：方向加速度 + 0.91 空气阻力，所以起步和停下都是平滑的；
 * 位置按 tick 推进，但对外提供按帧插值（partialTick）的位置与视角，避免 20Hz 跳变带来的卡顿感。</p>
 */
public class FreeCamState {
    /** 飞行加速度（格/tick²），与原版创造飞行一致 */
    private static final double FLY_ACCEL = 0.05;
    /** 疾跑时的加速度倍率 */
    private static final double SPRINT_MULTIPLIER = 2.0;
    /** 空气阻力，与原版一致 */
    private static final double DRAG = 0.91;

    private static boolean active = false;
    /** 本 tick 结束时的相机位置 */
    private static double x;
    private static double y;
    private static double z;
    /** 上一 tick 结束时的相机位置（用于帧间插值） */
    private static double prevX;
    private static double prevY;
    private static double prevZ;
    /** 当前速度（格/tick） */
    private static double vx;
    private static double vy;
    private static double vz;

    public static boolean isActive() {
        return active;
    }

    /** 帧插值后的相机 X（partialTick 为当前帧位于 tick 内的进度 0-1） */
    public static double getRenderX(float partialTick) {
        return prevX + (x - prevX) * partialTick;
    }

    public static double getRenderY(float partialTick) {
        return prevY + (y - prevY) * partialTick;
    }

    public static double getRenderZ(float partialTick) {
        return prevZ + (z - prevZ) * partialTick;
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
        prevX = x;
        prevY = y;
        prevZ = z;
        vx = 0;
        vy = 0;
        vz = 0;
        active = true;
        // 同步给服务端可读副本，使区块加载跟随相机
        FreeCamTracker.setActive(true, minecraft.player.getUUID(), x, y, z);
    }

    /**
     * 根据玩家输入移动自由相机（每个客户端 tick 调用一次）。
     *
     * <p>方向基于玩家当前视线，因此「看向哪就往哪飞」；空格上升、潜行下降、疾跑加速。
     * 加速度与阻力沿用原版创造/观察者飞行的数值，起步与停下都是平滑的。</p>
     */
    public static void update(Minecraft minecraft, Input input) {
        if (!active || minecraft.player == null) {
            return;
        }

        // 记录上一 tick 位置，供渲染时帧间插值
        prevX = x;
        prevY = y;
        prevZ = z;

        int forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
        int strafe = (input.right() ? 1 : 0) - (input.left() ? 1 : 0);
        int vertical = (input.jump() ? 1 : 0) - (input.shift() ? 1 : 0);

        if (forward != 0 || strafe != 0 || vertical != 0) {
            // 前向：直接用玩家视线向量（含俯仰）
            Vec3 look = minecraft.player.getLookAngle();
            // 水平右向量：由偏航角旋转得到（与视线保持垂直，且不随俯仰倾斜）
            double yawRad = Math.toRadians(minecraft.player.getYRot());
            double rightX = -Math.cos(yawRad);
            double rightZ = -Math.sin(yawRad);

            double dirX = look.x * forward + rightX * strafe;
            double dirY = look.y * forward + vertical;
            double dirZ = look.z * forward + rightZ * strafe;

            // 归一化：斜向移动不会比单一方向更快（与原版 moveRelative 的做法一致）
            double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
            if (length > 1.0E-4) {
                double accel = FLY_ACCEL * (input.sprint() ? SPRINT_MULTIPLIER : 1.0) / length;
                vx += dirX * accel;
                vy += dirY * accel;
                vz += dirZ * accel;
            }
        }

        // 空气阻力：松开按键后平滑滑行减速，而不是立刻停住
        vx *= DRAG;
        vy *= DRAG;
        vz *= DRAG;

        x += vx;
        y += vy;
        z += vz;
        // 持续同步位置，让服务端保持相机周围的区块加载
        FreeCamTracker.updatePosition(x, y, z);
    }
}
