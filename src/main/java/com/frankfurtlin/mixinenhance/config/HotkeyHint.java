package com.frankfurtlin.mixinenhance.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注「该配置项有对应快捷键」。
 *
 * <p>Cloth Config 里由 {@code HotkeyConfigScreen} 注册的 GUI 提供者接管：在原开关下面补一行
 * 「当前按键：F6」，值取当前真实绑定（改绑后会跟着变）。</p>
 *
 * <p>注解放在 common 包，客户端与服务端都能加载；只有渲染配置界面时才需要客户端的实现。</p>
 *
 * @author Frankfurtlin
 * @version 1.0
 * @date 2026/10/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface HotkeyHint {
    /** 快捷键 id，需与 {@code HotkeyRegistry.register(...)} 的第一个参数一致。 */
    String value();
}
