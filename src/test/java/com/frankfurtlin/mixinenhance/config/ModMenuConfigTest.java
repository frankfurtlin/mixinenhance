package com.frankfurtlin.mixinenhance.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 配置根的结构约束：每个字段都必须归属某个标签页。
 *
 * <p>踩过的坑：给 {@code ModMenuConfig} 加一个「无归属」字段（例如内部用的 {@code configVersion}）时，
 * Cloth 会额外生成一个 {@code category.default} 标签页 —— 界面里多出一个内容为空、且没有翻译的页签。
 * 字段就算标了 {@code Gui.Excluded} 也一样。所以这条约束用测试钉死。</p>
 *
 * @author Frankfurtlin
 * @date 2026/10/15
 */
class ModMenuConfigTest {
    @Test
    void 每个根字段都必须归属标签页() {
        for (Field field : ModMenuConfig.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            assertNotNull(field.getAnnotation(ConfigEntry.Category.class),
                () -> "字段 " + field.getName()
                    + " 缺少 @ConfigEntry.Category，Cloth 会多出一个未翻译的 default 标签页");
        }
    }

    @Test
    void 配置版本号不在界面显示() throws Exception {
        Field field = ModMenuConfig.class.getDeclaredField("configVersion");
        assertNotNull(field.getAnnotation(ConfigEntry.Gui.Excluded.class),
            "configVersion 是内部字段，不应出现在配置界面");
    }
}
