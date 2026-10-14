package com.frankfurtlin.mixinenhance;

import com.frankfurtlin.mixinenhance.config.ModMenuConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

/**
 * 配置门面：与运行端无关，客户端与专用服务端都可安全访问。
 *
 * <p>此前配置注册放在客户端入口类 {@code MixinEnhanceClient} 的静态块里，而该类的静态字段引用了客户端类
 * {@code KeyMapping}。服务端可达的 mixin（如 {@code ServerPlayerEntityMixin}）一旦调用它取配置，就会连带初始化
 * 那些客户端类，在专用服务端上存在 {@code NoClassDefFoundError} 风险。配置读取统一走本类即可规避。</p>
 *
 * <p>注册是惰性的：无论哪一侧、无论初始化顺序如何，第一次读取配置时都会自动完成迁移 + 注册。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/15
 */
public final class MixinEnhanceConfig {
    private static ConfigHolder<ModMenuConfig> holder;

    private MixinEnhanceConfig() {
    }

    /** 取当前配置；首次调用会先跑配置迁移，再注册配置持有者。 */
    public static ModMenuConfig get() {
        return holder().getConfig();
    }

    /** 取配置持有者（命令的 reload / save 需要）。 */
    public static ConfigHolder<ModMenuConfig> holder() {
        if (holder == null) {
            // 迁移必须早于 AutoConfig 读文件，否则旧配置会被当作「缺字段」直接补默认值
            try {
                ConfigMigrations.run();
            } catch (Throwable e) {
                MixinEnhance.LOGGER.warn("配置迁移未执行（不影响游戏）：{}", e.toString());
            }
            holder = AutoConfig.register(ModMenuConfig.class, JanksonConfigSerializer::new);
        }
        return holder;
    }

    /** 从磁盘重新加载配置（对应 {@code /mixinenhance reload}）。 */
    public static void reload() {
        holder().load();
    }

    /** 把当前配置写回磁盘。 */
    public static void save() {
        holder().save();
    }
}
