package com.frankfurtlin.mixinenhance;

import com.frankfurtlin.mixinenhance.event.Events;
import com.frankfurtlin.mixinenhance.selfcheck.InjectionSelfCheck;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frankfurtlin
 * @version 1.0
 * @date 2024/6/10 13:44
 */
public class MixinEnhance implements ModInitializer {
    /** 模组日志（客户端与专用服务端通用）。 */
    public static final Logger LOGGER = LoggerFactory.getLogger("MixinEnhance");

    @Override
    public void onInitialize() {
        // 预热配置：顺手完成配置迁移（必须在任何 mixin 读取配置之前）
        MixinEnhanceConfig.get();

        Events.initialize();

        // 注入自检：把「哪些注入点在本版本失效了」打到日志里（require = 0 的注入失败不会崩，只会静默失效）
        InjectionSelfCheck.init();

        // 游戏内命令：/mixinenhance status|recheck|reload|list|toggle
        MixinEnhanceCommand.register();
    }
}
