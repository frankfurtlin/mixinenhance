package com.frankfurtlin.mixinenhance.client;

import com.frankfurtlin.mixinenhance.MixinEnhanceConfig;

/**
 * 亮度调节（全亮）的运行时开关。
 *
 * <p>配置项 {@code playerConfig.enableGamma} 是总开关：关掉时快捷键不会有任何反应，全亮也立刻失效；
 * 开启时，进游戏**默认仍是原版亮度**，只有按下快捷键才切到全亮，再按一次回到原版亮度。
 * 开关状态只保留在本次游戏，重启后回到「默认不生效」。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/11
 */
public final class GammaState {
    /** 本次游戏是否已用快捷键打开全亮 */
    private static boolean enabled = false;

    private GammaState() {
    }

    /** 当前是否生效（配置开启 且 已按快捷键打开）。 */
    public static boolean isEnabled() {
        return enabled && MixinEnhanceConfig.get().playerConfig.enableGamma;
    }

    /**
     * 快捷键切换一次。
     *
     * @return 切换后是否生效；配置关闭时无反应，返回 false
     */
    public static boolean toggle() {
        if (!MixinEnhanceConfig.get().playerConfig.enableGamma) {
            return false;
        }
        enabled = !enabled;
        return enabled;
    }

    /** 配置被关掉时调用：清掉本次游戏的开关状态，避免重新开启配置后直接变全亮。 */
    public static void reset() {
        enabled = false;
    }
}
